package com.example.pastfinder.api

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException

class ApiClient(private val baseUrl: String) {
    private val client = OkHttpClient()

    private var userId: String? = null

    fun post(endpoint: String, jsonBody: String, callback: (Boolean, String) -> Unit) {
        val url = "$baseUrl$endpoint"
        val mediaType = "application/json".toMediaType()
        val body = jsonBody.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, e.message ?: "Unknown error")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    if (endpoint == "/members/login") {
                        val responseBody = response.body?.string()
                        if(responseBody != "-1")
                        {
                            userId = responseBody
                            callback(true, "성공!")
                        } else {
                            callback(false, "잘못된 아이디 혹은 비밀번호입니다!")
                        }
                    }

                } else {
                    callback(false, "Error: ${response.body}")
                }
            }
        })
    }

    // 일기와 리마인더를 추가하는 postElement 함수
    fun postElement(endpoint: String, jsonBody: String, callback: (Boolean, String) -> Unit) {
        val url = "$baseUrl$endpoint/${userId}"
        val mediaType = "application/json".toMediaType()
        val body = jsonBody.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, e.message ?: "Unknown error")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { callback(true, it) }
                } else {
                    callback(false, "Error: ${response.body?.string()}")
                }
            }
        })
    }

    // 일기와 리마인더 리스트 가져옴
    fun getElements(endpoint: String, callback: (Boolean, String) -> Unit) {
        val url = "$baseUrl$endpoint/${userId}"

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, e.message ?: "Unknown error")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    callback(true, response.body?.string() ?: "Success")
                } else {
                    callback(false, "Error: ${response.code}")
                }
            }
        })
    }

    fun delete(endpoint: String, jsonBody: String?, callback: (Boolean, String) -> Unit) {
        val url = "$baseUrl$endpoint"
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jsonBody?.toRequestBody(mediaType)

        val requestBuilder = Request.Builder()
            .url(url)
            .delete(body)

        val request = requestBuilder.build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, e.message ?: "Unknown error")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    callback(true, response.body?.string() ?: "Success")
                } else {
                    callback(false, "Error: ${response.code}")
                }
            }
        })
    }
}
