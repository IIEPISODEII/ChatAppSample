package com.example.chatappsample.domain.repository

import com.example.chatappsample.domain.`interface`.*
import com.example.chatappsample.domain.dto.ChatRoomDomain
import com.example.chatappsample.domain.dto.UserDomain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    /** 내 유저 정보 가져오기
     * @param listener 정보를 가져올 경우 콜백 등록
     * **/
    fun fetchCurrentUser(
        listener: OnGetDataListener
    )

    fun fetchUserListFromExternalDB(
        coroutineScope: CoroutineScope
    )

    /** 모든 유저 정보 가져오기
     * @param listener 정보를 가져올 경우 콜백 등록
     * **/
    suspend fun fetchUserListFromRoomDB(): Flow<List<UserDomain>>

    fun signIn(
        email: String,
        password: String,
        listener: OnSignInListener
    )

    /** 로그아웃 **/
    fun signOut(): Boolean

    /** 유저 정보 생성
     * @param name 유저 닉네임
     * @param email 유저 이메일
     * @param password 유저 비밀번호
     * @param listener 유저 정보 등록할 경우 콜백 등록
     * **/
    fun sendVerificationEmail(
        email: String,
        password: String,
        listener: OnSendEmailVerificationListener
    )

    fun signUp(
        name: String = "",
        listener: OnEmailVerificationListener
    )

    /** 파이어베이스 데이터베이스에 유저 정보 저장
     * @param userDomain 저장할 유저 정보
     * @param changeProfileImage 프로필 이미지 갱신 여부. true면 갱신, false면 갱신하지 않음
     * **/
    fun updateCurrentUser(
        userDomain: UserDomain,
        changeProfileImage: Boolean
    )

    /** 프로필 이미지 다운로드
     * @param userID 유저ID
     * @param onFileDownloadListener 파일 다운로드할 경우 콜백 등록
     */
    fun downloadProfileImage(
        userID: String,
        onFileDownloadListener: OnFileDownloadListener
    )

    /**
     * 채팅방 상태 업데이트
     *
     * @param myId 내 아이디
     * @param yourId 상대방 아이디
     * @param time 내 접속 시간
     * @param onSuccess 채팅방 업데이트 성공 시 콜백
     * @param onFail 채팅방 업데이트 실패 시 콜백
     * @param enter true: 채팅방 들어오기, false: 채팅방 나가기
     * @param coroutineScope 코루틴스코프 설정
     */
    fun updateChatRoomState(
        myId: String,
        yourId: String,
        time: String,
        onSuccess: (String) -> Unit,
        onFail: () -> Unit,
        enter: Boolean,
        coroutineScope: CoroutineScope
    )

    suspend fun fetchChatRoomList(
        currentUserId: String
    ): Flow<List<ChatRoomDomain>>
}