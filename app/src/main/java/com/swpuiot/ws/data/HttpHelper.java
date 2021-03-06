package com.swpuiot.ws.data;

import android.util.Log;

import com.swpuiot.ws.App;
import com.swpuiot.ws.constant.Constants;
import com.swpuiot.ws.entities.response.ForecastResponse;
import com.swpuiot.ws.entities.response.FullInfoResponse;
import com.swpuiot.ws.entities.response.HourlyResponse;
import com.swpuiot.ws.entities.response.SuggestResponse;
import com.swpuiot.ws.entities.response.TomorrowForestResponse;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import top.wuhaojie.lib.http.RetrofitHttpHelper;
import top.wuhaojie.lib.utils.PreferenceUtils;

/**
 * Author: wuhaojie
 * E-mail: w19961009@126.com
 * Date: 2017/06/18 12:18
 * Version: 1.0
 */

public class HttpHelper {

    public static final String TAG = "HttpHelper";
    private volatile static HttpHelper mHttpHelper;

    private final CommonApi mCommonApi;
    private final WeatherNetApi mWeatherNetApi;

    private HttpHelper() {
        String commonUrl = PreferenceUtils.getInstance(App.getContext()).getStringParam(Constants.CONFIG_KEY.NORMAL_SERVER, CommonApi.BASE_URL);
        mCommonApi = new RetrofitHttpHelper<>(commonUrl, CommonApi.class).getService();
        mWeatherNetApi = new RetrofitHttpHelper<>(WeatherNetApi.BASE_URL, WeatherNetApi.class).getService();
    }

    public static HttpHelper get() {
        if (mHttpHelper == null) {
            synchronized (HttpHelper.class) {
                if (mHttpHelper == null) {
                    mHttpHelper = new HttpHelper();
                }
            }
        }
        return mHttpHelper;
    }


    public void forecast(String city, String language, final Action1<? super ForecastResponse> onNext) {
        mWeatherNetApi
                .forecast(city, WeatherNetApi.KEY, language)
                .compose(new TransThread<>())
                .subscribe(onNext, mErrorHandler);
    }

    public void suggestion(String city, String language, final Action1<? super SuggestResponse> onNext) {
        mWeatherNetApi
                .suggestion(city, WeatherNetApi.KEY, language)
                .compose(new TransThread<>())
                .subscribe(onNext, mErrorHandler);
    }

    public void hourly(String city, final Action1<? super HourlyResponse> onNext) {
        mWeatherNetApi
                .hourly(city, WeatherNetApi.KEY)
                .compose(new TransThread<>())
                .subscribe(onNext, mErrorHandler);
    }

    public void fullInfo(final Action1<? super FullInfoResponse> onNext) {
        mCommonApi
                .fullInfo()
                .compose(new TransThread<>())
                .subscribe(onNext, mErrorHandler);
    }

    public void tomorrow(final Action1<? super TomorrowForestResponse> onNext) {
        mCommonApi
                .tomorrow()
                .compose(new TransThread<>())
                .subscribe(onNext, mErrorHandler);
    }

    private OnError mErrorHandler = new OnError();

    private class OnError implements Action1<Throwable> {

        @Override
        public void call(Throwable throwable) {
            Log.d(TAG, "call: 访问错误!" + throwable.getMessage());
        }
    }

    private class TransThread<H> implements Observable.Transformer<H, H> {

        @Override
        public Observable<H> call(Observable<H> hObservable) {
            return hObservable.observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io());
        }
    }

}
