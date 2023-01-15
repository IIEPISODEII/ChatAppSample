package com.example.chatappsample.domain.repository

import com.example.chatappsample.domain.dto.ChatroomDomain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface ChatroomRepository {
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
    fun updateChatroomState(
        myId: String,
        yourId: String,
        time: String,
        onSuccess: (String) -> Unit,
        onFail: () -> Unit,
        enter: Boolean
    )

    fun fetchChatroomListFromExternalDB(
        currentUserId: String,
        coroutineScope: CoroutineScope
    )

    suspend fun fetchChatroomListFromRoom(
        currentUserId: String
    ): Flow<List<ChatroomDomain>>

    fun fetchReaderLogFromExternalDB(
        chatroomId: String,
        currentUserId: String,
        coroutineScope: CoroutineScope
    )

    suspend fun fetchReaderLogFromRoom(
        chatroomId: String
    ): List<ChatroomDomain.ReaderLogDomain>

    suspend fun fetchReaderLogFromRoomAsFlow(
        chatroomId: String
    ): Flow<List<ChatroomDomain.ReaderLogDomain>>
}