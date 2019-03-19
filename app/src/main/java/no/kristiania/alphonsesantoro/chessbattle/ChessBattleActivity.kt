package no.kristiania.alphonsesantoro.chessbattle

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_chess_battle.*
import kotlinx.android.synthetic.main.drawer_header.view.*
import kotlinx.android.synthetic.main.games_in_progress.view.*
import no.kristiania.alphonsesantoro.chessbattle.adapters.GamesInProgressRecyclerAdapter
import no.kristiania.alphonsesantoro.chessbattle.database.GameRepository
import no.kristiania.alphonsesantoro.chessbattle.game.Color
import no.kristiania.alphonsesantoro.chessbattle.game.GameMode.*
import no.kristiania.alphonsesantoro.chessbattle.game.GameStatus
import no.kristiania.alphonsesantoro.chessbattle.viewmodels.SharedViewModel

class ChessBattleActivity : AppCompatActivity() {

    lateinit var drawerLayout: DrawerLayout
    val auth = FirebaseAuth.getInstance()
    var firebaseUser = auth.currentUser
    lateinit var navigationView: NavigationView
    lateinit var sharedViewModel: SharedViewModel
    lateinit var repository: GameRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chess_battle)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp)
            setBackgroundDrawable(resources.getDrawable(R.drawable.gradient_fade_primary, theme))
        }

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)

        sharedViewModel = ViewModelProviders.of(this).get(SharedViewModel::class.java)
        logIn(firebaseUser?.email)

        setNavigationListener()
    }

    private fun logIn(email: String?) {
        Thread {
            sharedViewModel.setUser(email).join() // Wait for user to be set
            runOnUiThread { showGamesInProgress() }
        }.start()
    }

    private fun showGamesInProgress() {
        repository = GameRepository(application, sharedViewModel.user!!.id!!)
        repository.getUsersGames().observe(this, Observer { list ->
            if (list.isNotEmpty()) {
                games_in_progress_action.setOnClickListener {
                    val inflater = LayoutInflater.from(this)
                    val gameView = inflater.inflate(R.layout.games_in_progress, null)
                    val alert = AlertDialog.Builder(this)
                    alert.setView(gameView)
                    alert.setTitle("Games in Progress")
                    val alertdialog = alert.create()
                    runOnUiThread {
                        number_of_games_ip.text = list.count().toString()
                        alertdialog.show()
                        with(gameView.games_in_progress_recycler) {
                            layoutManager = LinearLayoutManager(context)
                            adapter =
                                GamesInProgressRecyclerAdapter(list, this@ChessBattleActivity, alertdialog)
                        }
                    }
                }
            } else {
                runOnUiThread { number_of_games_ip.text = "" }
                games_in_progress_action.setOnClickListener(null)
            }
        })
    }

    private fun setNavigationListener() {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            drawerLayout.closeDrawers()
            when (menuItem.itemId) {
                R.id.live_game -> findNavController(R.id.fragment).navigate(
                    R.id.findGameFragment,
                    bundleOf("gameMode" to LIVE, "gameId" to -1L)
                )
                R.id.computer_game -> findNavController(R.id.fragment).navigate(
                    R.id.boardFragment,
                    bundleOf("gameMode" to STOCKFISH, "perspective" to Color.random, "gameId" to -1L)
                )
                R.id.local_two_player -> findNavController(R.id.fragment).navigate(
                    R.id.boardFragment,
                    bundleOf("gameMode" to TWO_PLAYER, "perspective" to Color.WHITE, "gameId" to -1L)
                )
                R.id.mainMenuFragment -> findNavController(R.id.fragment).navigate(
                    R.id.boardFragment,
                    bundleOf()
                )
                R.id.signIn -> findNavController(R.id.fragment).navigate(R.id.signInFragment, bundleOf())
                R.id.signOut -> {
                    auth.signOut()
                    firebaseUser = auth.currentUser
                    sharedViewModel.setUser(SharedViewModel.defaultEmail)
                }
            }
            true
        }
    }

    /**
     * When Signed In show only user specific functionality
     * I.e. Only a signed in user can play online
     */
    private fun showSignInItems(showSignIn: Boolean) {
        with(navigationView.menu) {
            findItem(R.id.signIn).isVisible = showSignIn
            findItem(R.id.signOut).isVisible = !showSignIn
            findItem(R.id.live_game).isVisible = !showSignIn
        }
    }

    private fun showMenuItems() {
        if (sharedViewModel.user?.email == SharedViewModel.defaultEmail) {
            showSignInItems(true)
            drawerLayout.userNameHeader.text = ""
        } else {
            showSignInItems(false)
            drawerLayout.userNameHeader.text = sharedViewModel.user?.userName
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
                showMenuItems()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
