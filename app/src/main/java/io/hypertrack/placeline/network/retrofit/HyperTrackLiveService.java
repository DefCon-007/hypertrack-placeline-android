
/*
The MIT License (MIT)

Copyright (c) 2015-2017 HyperTrack (http://hypertrack.com)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package io.hypertrack.placeline.network.retrofit;

import com.hypertrack.lib.models.Action;
import com.hypertrack.lib.models.User;

import io.hypertrack.placeline.model.AcceptInviteModel;
import io.hypertrack.placeline.model.VerifyCodeModel;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by piyush on 22/10/16.
 */
public interface HyperTrackLiveService {

    @POST("users/{id}/accept_invite/")
    Call<User> acceptInvite(@Path("id") String id, @Body AcceptInviteModel acceptInviteModel);

    @POST("users/{id}/validate_code/")
    Call<User> validateCode(@Path("id") String id, @Body VerifyCodeModel verifyCodeModel);

    @POST("users/{id}/send_verification/")
    Call<User> sendCode(@Path("id") String id);

    @GET("actions/")
    Call<Action> getActionForShortCode(@Query("short_code") String shortCode );
}