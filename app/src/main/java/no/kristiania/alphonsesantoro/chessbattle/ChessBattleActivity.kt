package no.kristiania.alphonsesantoro.chessbattle

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.navigation.NavigationView
import no.kristiania.alphonsesantoro.chessbattle.game.Color


class ChessBattleActivity : AppCompatActivity() {
    lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chess_battle)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val actionbar: ActionBar? = supportActionBar
        actionbar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp)
        }

        drawerLayout = findViewById(R.id.drawer_layout)

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            drawerLayout.closeDrawers()
            when (menuItem.itemId) {
                R.id.live_game -> findNavController(R.id.fragment).navigate(
                    R.id.findGameFragment,
                    bundleOf("live" to true)
                )
                R.id.computer_game -> findNavController(R.id.fragment).navigate(
                    R.id.boardFragment,
                    bundleOf("stockfish" to true, "perspective" to Color.WHITE)
                )
                R.id.local_two_player -> findNavController(R.id.fragment).navigate(
                    R.id.boardFragment,
                    bundleOf("two_player" to true, "perspective" to Color.WHITE)
                )

            }
            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2) {
            findNavController(R.id.fragment).navigate(R.id.findGameFragment, bundleOf())
        }
    }
}
