package no.kristiania.alphonsesantoro.chessbattle.viewmodels

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.games.GamesCallbackStatusCodes
import com.google.android.gms.games.RealTimeMultiplayerClient
import com.google.android.gms.games.multiplayer.realtime.OnRealTimeMessageReceivedListener
import com.google.android.gms.games.multiplayer.realtime.Room
import com.google.android.gms.games.multiplayer.realtime.RoomConfig
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateCallback
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateCallback
import com.google.firebase.auth.*
import com.google.gson.Gson
import jstockfish.Uci
import no.kristiania.alphonsesantoro.chessbattle.database.UserModel
import no.kristiania.alphonsesantoro.chessbattle.database.UserRepository
import no.kristiania.alphonsesantoro.chessbattle.game.GameState

class SharedViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "SharedViewModel"

    var user: UserModel? = null
    val auth = FirebaseAuth.getInstance()
    var repository = UserRepository(application)
    lateinit var mRoomConfig: RoomConfig
    var white: String? = null
    var black: String? = null
    var whiteId: String? = null
    var blackId: String? = null
    var roomId: String? = null
    var acceptedGame: Boolean = false
    var declinedGame: Boolean = false
    var listener: OnMessageReceivedListener? = null
    lateinit var mRealTimeMultiplayerClient: RealTimeMultiplayerClient

    fun init(listener: OnMessageReceivedListener) {
        this.listener = listener
    }

    internal fun setUser(email: String?, account: GoogleSignInAccount? = null): Thread {
        val thread = Thread {
            val mail = email ?: defaultEmail
            user = repository.findUserByEmail(mail)
            if (user == null && auth.currentUser != null) {
                user = UserModel(
                    userName = auth.currentUser!!.displayName!!,
                    email = auth.currentUser!!.email!!,
                    firebase_key = auth.currentUser?.uid,
                    google_play_key = account?.id
                )
                repository.insert(user!!)
                user = repository.findUserByEmail(mail)
            } else {
                // Update user if nessecary
                if(account?.id != null) user!!.google_play_key = account.id!!
                repository.update(user!!)
            }
            val sharedPreferences = getApplication<Application>().getSharedPreferences("User", MODE_PRIVATE)
            sharedPreferences.edit {
                putString("email", email)
            }
        }
        thread.start()
        return thread
    }

    internal fun signInWithFirebase(acct: GoogleSignInAccount) {
        val credential = PlayGamesAuthProvider.getCredential(acct.serverAuthCode!!)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    if (auth.currentUser?.email == null) {
                        Log.d("SignIn", "Email: " + acct.email!!)
                        auth.currentUser!!.updateEmail(acct.email!!).addOnCompleteListener {
                            if(it.isSuccessful){
                                setUser(auth.currentUser!!.email, acct)
                            } else {
                                setUser(defaultEmail) // Sign in might be successful but was unable to save email
                            }
                        }
                    } else {
                        setUser(auth.currentUser!!.email, acct)
                    }
                } else {
                    Log.w("SignIn", "signInWithCredential:failure", task.exception)
                }
            }
    }

    interface OnMessageReceivedListener {
        fun onMessageReceived(gameState: GameState)
    }

    val mMessageReceivedHandler = OnRealTimeMessageReceivedListener { message ->
        Log.d(TAG, "Message: ${String(message.messageData)}")
        val gameState = Gson().fromJson(String(message.messageData), GameState::class.java)
        Log.d(TAG, gameState.toString())
        listener?.onMessageReceived(gameState)
        white = gameState.white
        black = gameState.black
        whiteId = gameState.whiteId
        blackId = gameState.blackId
        acceptedGame = true
    }

    var pendingMessageSet: HashSet<Int> = HashSet()

    @Synchronized
    fun recordMessageToken(tokenId: Int) {
        pendingMessageSet.add(tokenId)
    }

    val handleMessageSentCallback = object : RealTimeMultiplayerClient.ReliableMessageSentCallback {
        override fun onRealTimeMessageSent(statusCode: Int, tokenId: Int, recipientId: String) {
            // handle the message being sent.
            synchronized(this) {
                pendingMessageSet.remove(tokenId)
            }
        }
    }


    val mRoomUpdateCallback = object : RoomUpdateCallback() {
        override fun onRoomCreated(code: Int, room: Room?) {
            // Update UI and internal state based on room updates.
            if (code == GamesCallbackStatusCodes.OK && room != null) {
                Log.d(TAG, "Room " + room.roomId + " created.")
                roomId = room.roomId
                val players = room.participants.map { it.displayName }
                white = players.random()
                black = players.first { it != white }
                Log.d(TAG, "White: $white")
                Log.d(TAG, "Black: $black")

            } else {
                Log.w(TAG, "Error creating room: $code")
            }
        }

        override fun onJoinedRoom(code: Int, room: Room?) {
            // Update UI and internal state based on room updates.
            if (code == GamesCallbackStatusCodes.OK && room != null) {
                Log.d(TAG, "Room " + room.roomId + " joined.")
            } else {
                Log.w(TAG, "Error joining room: $code")
            }
        }

        override fun onLeftRoom(code: Int, roomId: String) {
            Log.d(TAG, "Left room$roomId")
        }

        override fun onRoomConnected(code: Int, room: Room?) {
            if (code == GamesCallbackStatusCodes.OK && room != null) {
                Log.d(TAG, "Room " + room.roomId + " connected.")
            } else {
                Log.w(TAG, "Error connecting to room: $code")
            }
        }
    }

    val mRoomStatusCallbackHandler = object : RoomStatusUpdateCallback() {
        override fun onRoomConnecting(room: Room?) {
            Log.d("RoomStatusCallback", "onRoomConnecting: Room: ${room?.roomId}")
        }

        override fun onP2PConnected(message: String) {
            Log.d("RoomStatusCallback", "onP2PConnected: $message")
        }

        override fun onDisconnectedFromRoom(room: Room?) {
            Log.d("RoomStatusCallback", "onDisconnectedFromRoom: Room: ${room?.roomId}")
        }

        override fun onPeerDeclined(room: Room?, p1: MutableList<String>) {
            Log.d("RoomStatusCallback", "onPeerDeclined: Room: ${room?.roomId} List: $p1")
        }

        override fun onPeersConnected(room: Room?, p1: MutableList<String>) {
            Log.d("RoomStatusCallback", "onPeersConnected: Room: ${room?.roomId} List: $p1")
        }

        override fun onPeerInvitedToRoom(room: Room?, p1: MutableList<String>) {
            Log.d("RoomStatusCallback", "onPeerInvitedToRoom: Room: ${room?.roomId} List: $p1")
        }

        override fun onPeerLeft(room: Room?, p1: MutableList<String>) {
            Log.d("RoomStatusCallback", "onPeerLeft: Room: ${room?.roomId} List: $p1")
        }

        override fun onRoomAutoMatching(room: Room?) {
            Log.d("RoomStatusCallback", "onRoomAutoMatching: Room: ${room?.roomId}")
        }

        override fun onPeerJoined(room: Room?, p1: MutableList<String>) {
            Log.d("RoomStatusCallback", "onPeerJoined: Room: ${room?.roomId} List: $p1")
        }

        override fun onConnectedToRoom(room: Room?) {
            Log.d("RoomStatusCallback", "onConnectedToRoom: Room: ${room!!.roomId}")

            roomId = room.roomId
            if(white != null && black != null){
                whiteId = room.participants?.first { it.displayName == white }!!.participantId
                blackId = room.participants?.first { it.displayName == black }!!.participantId
                val gameState = GameState(
                    fen = Uci.fen(),
                    white = white!!,
                    black = black!!,
                    whiteId = whiteId!!,
                    blackId = blackId!!,
                    roomId = roomId
                )
                room.participants.forEach { p ->
                    mRealTimeMultiplayerClient.sendReliableMessage(
                        Gson().toJson(gameState).toByteArray(),
                        room.roomId,
                        p.participantId,
                        handleMessageSentCallback
                    ).addOnCompleteListener {
                        recordMessageToken(it.result!!)
                    }
                }
                acceptedGame = true
            }
        }

        override fun onPeersDisconnected(room: Room?, p1: MutableList<String>) {
            Log.d("RoomStatusCallback", "onPeersDisconnected: Room: ${room?.roomId} List: $p1")
        }

        override fun onP2PDisconnected(message: String) {
            Log.d("RoomStatusCallback", "onP2PDisconnected: $message")
        }
    }

    companion object {
        const val defaultEmail = "default@chessbattle.com"
    }
}