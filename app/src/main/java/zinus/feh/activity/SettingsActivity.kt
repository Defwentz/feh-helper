package zinus.feh.activity

import android.annotation.TargetApi
import android.content.Context
import android.content.res.Configuration
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.*
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import com.afollestad.materialdialogs.MaterialDialog
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.runOnUiThread
import org.jetbrains.anko.toast
import zinus.feh.DataOp
import zinus.feh.Helper
import zinus.feh.R
import zinus.feh.database


/**
 * A [PreferenceActivity] that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 *
 * See [Android Design: Settings](http://developer.android.com/design/patterns/settings.html)
 * for design guidelines and the [Settings API Guide](http://developer.android.com/guide/topics/ui/settings.html)
 * for more information on developing a Settings UI.
 */
class SettingsActivity : AppCompatPreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setupActionBar()
    }

    /**
     * Set up the [android.app.ActionBar], if the API is available.
     */
    private fun setupActionBar() {
        val root = findViewById<View>(android.R.id.list).getParent().getParent().getParent() as LinearLayout
        val toolbar = layoutInflater.inflate(R.layout.toolbar, root, false) as android.support.v7.widget.Toolbar
        root.addView(toolbar, 0)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    override fun onMenuItemSelected(featureId: Int, item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            else -> {
                return true
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    override fun onIsMultiPane(): Boolean {
        return isXLargeTablet(this)
    }

    /**
     * {@inheritDoc}
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    override fun onBuildHeaders(target: List<PreferenceActivity.Header>) {
        loadHeadersFromResource(R.xml.pref_headers, target)
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    override fun isValidFragment(fragmentName: String): Boolean {
        return PreferenceFragment::class.java.name == fragmentName
                || GeneralPreferenceFragment::class.java.name == fragmentName
                || DataSyncPreferenceFragment::class.java.name == fragmentName
                || NotificationPreferenceFragment::class.java.name == fragmentName
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class GeneralPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_general)
            setHasOptionsMenu(true)

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("example_text"))
            bindPreferenceSummaryToValue(findPreference("example_list"))
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                //startActivity(Intent(activity, SettingsActivity::class.java))
                return true
            }
            return super.onOptionsItemSelected(item)
        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class NotificationPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_notification)
            setHasOptionsMenu(true)

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"))
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                //startActivity(Intent(activity, SettingsActivity::class.java))
                return true
            }
            return super.onOptionsItemSelected(item)
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class DataSyncPreferenceFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_data_sync)
            setHasOptionsMenu(true)

            val updateBtn = findPreference(getString(R.string.key_update_local))
            updateBtn.setOnPreferenceClickListener { MaterialDialog.Builder(activity)
                    .title(R.string.title_update_local)
                    .content(R.string.summ_update_local)
                    .positiveText(R.string.yes)
                    .negativeText(R.string.no)
                    .onPositive { dialog, which ->
                        doAsync {
                            if (Helper.isNetworkConnected(activity)) {
                                DataOp.fetchFromGamepedia(activity, { heroes ->
                                    DataOp.clearLocal(activity.database)
                                    DataOp.saveToLocal(activity.database, heroes)
                                })
                            } else {
                                runOnUiThread { toast("no Internet acess") }
                            }
                        }
                    }
                    .show()
                true
            }
            val updateNewBtn = findPreference(getString(R.string.key_update_new_local))
            updateNewBtn.setOnPreferenceClickListener { MaterialDialog.Builder(activity)
                    .title(R.string.title_update_new_local)
                    .content(R.string.summ_update_new_local)
                    .positiveText(R.string.yes)
                    .negativeText(R.string.no)
                    .onPositive { dialog, which ->
                        doAsync {
                            if (Helper.isNetworkConnected(activity) && DataOp.heroes != null) {
                                DataOp.fetchNewFromGamepedia(activity,
                                        DataOp.fetchHeroNames(DataOp.heroes!!),
                                        { heroes ->
                                    DataOp.clearLocal(activity.database)
                                    DataOp.saveToLocal(activity.database, heroes)
                                })
                            } else {
                                runOnUiThread { toast("no Internet acess") }
                            }
                        }
                    }
                    .show()
                true
            }
            val wipeBtn = findPreference(getString(R.string.key_wipe_nation))
            wipeBtn.setOnPreferenceClickListener { MaterialDialog.Builder(activity)
                        .title(R.string.title_wipe_nation)
                        .content(R.string.summ_wipe_nation)
                        .positiveText(R.string.yes)
                        .negativeText(R.string.no)
                        .onPositive { dialog, which ->
                            doAsync {
                                DataOp.clearNation(activity.database)
                                runOnUiThread { toast("wiped") }
                            }
                        }
                        .show()
                true
            }
            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            //bindPreferenceSummaryToValue(findPreference("sync_frequency"))
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                //startActivity(Intent(activity, SettingsActivity::class.java))
                return true
            }
            return super.onOptionsItemSelected(item)
        }
    }

    companion object {

        /**
         * A preference value change listener that updates the preference's summary
         * to reflect its new value.
         */
        private val sBindPreferenceSummaryToValueListener = Preference.OnPreferenceChangeListener { preference, value ->
            val stringValue = value.toString()

            if (preference is ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                val listPreference = preference
                val index = listPreference.findIndexOfValue(stringValue)

                // Set the summary to reflect the new value.
                preference.setSummary(
                        if (index >= 0)
                            listPreference.entries[index]
                        else
                            null)

            } else if (preference is RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent)

                } else {
                    val ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue))

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null)
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        val name = ringtone.getTitle(preference.getContext())
                        preference.setSummary(name)
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.summary = stringValue
            }
            true
        }

        /**
         * Helper method to determine if the device has an extra-large screen. For
         * example, 10" tablets are extra-large.
         */
        private fun isXLargeTablet(context: Context): Boolean {
            return context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_XLARGE
        }

        /**
         * Binds a preference's summary to its value. More specifically, when the
         * preference's value is changed, its summary (line of text below the
         * preference title) is updated to reflect the value. The summary is also
         * immediately updated upon calling this method. The exact display format is
         * dependent on the type of preference.

         * @see .sBindPreferenceSummaryToValueListener
         */
        private fun bindPreferenceSummaryToValue(preference: Preference) {
            // Set the listener to watch for value changes.
            preference.onPreferenceChangeListener = sBindPreferenceSummaryToValueListener

            // Trigger the listener immediately with the preference's
            // current value.
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.context)
                            .getString(preference.key, ""))
        }
    }
}
