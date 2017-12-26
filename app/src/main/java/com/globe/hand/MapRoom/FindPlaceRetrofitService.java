package com.globe.hand.MapRoom;

import com.google.android.gms.location.places.Place;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by ssangwoo on 2017-12-25.
 */

public interface FindPlaceRetrofitService {

    String baseUrl = "https://maps.googleapis.com/maps/";

    @GET("api/place/nearbysearch/json")
    Call<Place> getPlaceInfo(@Query("location") String location,
                             @Query("radius") int radius,
                             @Query("key") String key);
}
