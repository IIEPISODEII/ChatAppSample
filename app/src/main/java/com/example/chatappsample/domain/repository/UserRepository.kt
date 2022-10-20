package com.example.chatappsample.domain.repository

import com.example.chatappsample.domain.`interface`.OnFileDownloadListener
import com.example.chatappsample.domain.`interface`.OnGetDataListener
import com.example.chatappsample.domain.`interface`.OnGetRegistrationListener
import com.example.chatappsample.domain.dto.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    /** 내 유저 정보 가져오기
     * @param listener 정보를 가져올 경우 콜백 등록
     * **/
    fun getCurrentUser(listener: OnGetDataListener)

    fun receiveAllUsersFromExternalDB(coroutineScope: CoroutineScope)

    /** 모든 유저 정보 가져오기
     * @param listener 정보를 가져올 경우 콜백 등록
     * **/
    suspend fun getAllUsersFromRoomDB(): Flow<List<User>>

    /** 로그아웃 **/
    fun signOut(): Boolean

    /** 유저 정보 생성
     * @param name 유저 닉네임
     * @param email 유저 이메일
     * @param password 유저 비밀번호
     * @param listener 유저 정보 등록할 경우 콜백 등록
     * **/
    fun signUp(name: String, email: String, password: String, listener: OnGetRegistrationListener)

    /** 파이어베이스 데이터베이스에 유저 정보 저장
     * @param user 저장할 유저 정보
     * @param changeProfileImage 프로필 이미지 갱신 여부. true면 갱신, false면 갱신하지 않음
     * **/
    fun updateCurrentUser(user: User, changeProfileImage: Boolean)

    /** 프로필 이미지 다운로드
     * @param userID 유저ID
     * @param onFileDownloadListener 파일 다운로드할 경우 콜백 등록
     */
    fun downloadProfileImage(userID: String, onFileDownloadListener: OnFileDownloadListener)
}