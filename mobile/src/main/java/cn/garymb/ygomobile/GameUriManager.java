package cn.garymb.ygomobile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.util.Locale;

import cn.garymb.ygodata.YGOGameOptions;
import cn.garymb.ygomobile.bean.Deck;
import cn.garymb.ygomobile.lite.R;
import cn.garymb.ygomobile.ui.cards.DeckManagerActivity;
import cn.garymb.ygomobile.ui.preference.SettingsActivity;
import cn.garymb.ygomobile.utils.FileUtils;
import cn.garymb.ygomobile.utils.IOUtils;
import ocgcore.DataManager;

import static cn.garymb.ygomobile.Constants.ACTION_OPEN_DECK;
import static cn.garymb.ygomobile.Constants.ACTION_OPEN_GAME;
import static cn.garymb.ygomobile.Constants.CORE_REPLAY_PATH;
import static cn.garymb.ygomobile.Constants.CORE_SINGLE_PATH;
import static cn.garymb.ygomobile.Constants.QUERY_NAME;
import static cn.garymb.ygomobile.Constants.REQUEST_SETTINGS_CODE;


public class GameUriManager {
    private Activity activity;
    private String fname;

    public GameUriManager(Activity activity) {
        this.activity = activity;
    }

    public boolean doIntent(Intent intent) {
        Log.i("ygo", "doIntent");
        if (ACTION_OPEN_DECK.equals(intent.getAction())) {
            if (intent.getData() != null) {
                doUri(intent.getData());
            } else {
                String name = intent.getStringExtra(Intent.EXTRA_TEXT);
                doOpenPath(name);
            }
        } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            if (intent.getData() != null) {
                doUri(intent.getData());
            } else {
                activity.finish();
            }
        } else if (ACTION_OPEN_GAME.equals(intent.getAction())) {
            try {
                YGOGameOptions options = new YGOGameOptions();
                options.mServerAddr = intent.getStringExtra(Constants.QUERY_HOST);
                options.mUserName = intent.getStringExtra(Constants.QUERY_USER);
                options.mPort = intent.getIntExtra(Constants.QUERY_PORT, 0);
                options.mRoomName = intent.getStringExtra(Constants.QUERY_ROOM);
                YGOStarter.startGame(getActivity(), options);
            } catch (Exception e) {
                Toast.makeText(getActivity(), R.string.start_game_error, Toast.LENGTH_SHORT).show();
                activity.finish();
            }
        } else {
            return false;
        }
        return true;
    }

    public Activity getActivity() {
        return activity;
    }


    private String getPathName(String path, boolean withOutEx) {
        Log.d("ygo", "path=" + path);
        if (path != null) {
            int index = path.lastIndexOf("/");
            if (index > 0) {
                String name = path.substring(index + 1);
                if (withOutEx) {
                    index = name.lastIndexOf(".");
                    if (index > 0) {
                        //1.ydk
                        name = name.substring(0, index);
                    }
                }
                return name;
            }
        }
        return "tmp_" + System.currentTimeMillis();
    }

    private File getDeckFile(File dir, String name) {
        File file = new File(dir, name + ".ydk");
        if (file.exists()) {
            for (int i = 2; i < 10; i++) {
                file = new File(dir, name + "(" + i + ").ydk");
                if (!file.exists()) {
                    return file;
                }
            }
            return new File(dir, "tmp_" + System.currentTimeMillis() + ".ydk");
        } else {
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }
        return file;
    }

    private boolean isDeckDir(File file) {
        if (!Constants.COPY_YDK_FILE) {
            return true;
        }
        String deck = new File(AppsSettings.get().getDeckDir()).getAbsolutePath();
        return TextUtils.equals(deck, file.getParentFile().getAbsolutePath());
    }

    private File toLocalFile(Uri uri) {
        String path = uri.getPath();
        File remoteFile = null;
        if ("file".equals(uri.getScheme())) {
            remoteFile = new File(uri.getPath());
            if (getActivity().getApplicationInfo().targetSdkVersion > 28) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                        intent.setData(Uri.parse("package:$packageName"));
                        getActivity().startActivityForResult(intent, REQUEST_SETTINGS_CODE);
                        return null;
                    }
                }
            }
            try {
                if (!remoteFile.canRead()) {
                    Log.w("ygo", "don't read file " + remoteFile.getAbsolutePath());
                    return null;
                }
            } catch (Throwable e) {
                Log.e("ygo", "don't read file " + remoteFile.getAbsolutePath(), e);
                return null;
            }
        }
        String name = getPathName(path, false);
        File local;
        if (name.toLowerCase(Locale.US).endsWith(".ydk")) {
            File dir = Constants.COPY_YDK_FILE ? new File(AppsSettings.get().getDeckDir()) : new File(getActivity().getApplicationInfo().dataDir, "cache");
            local = getDeckFile(dir, name);
        } else if (name.toLowerCase(Locale.US).endsWith(".ypk")) {
            local = new File(AppsSettings.get().getExpansionsPath(), name);
        } else if (name.toLowerCase(Locale.US).endsWith(".yrp")) {
            local = new File(AppsSettings.get().getResourcePath() + "/" + CORE_REPLAY_PATH, name);
        } else if (name.toLowerCase(Locale.US).endsWith(".lua")) {
            local = new File(AppsSettings.get().getResourcePath() + "/" + CORE_SINGLE_PATH, name);
        } else {
            local = new File(AppsSettings.get().getResourcePath() + "/temp", name);
        }
        if (local.exists()) {
            Log.w("ygo", "Overwrite file " + local.getAbsolutePath());
        }
        if (remoteFile != null && TextUtils.equals(remoteFile.getAbsolutePath(), local.getAbsolutePath())) {
            //is same path
            Log.i("ygo", "is same file " + remoteFile.getAbsolutePath() + "==" + local.getAbsolutePath());
            return local;
        }
        //copy
        ParcelFileDescriptor pfd = null;
        FileInputStream input = null;
        try {
            File dir = local.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }
            if (remoteFile != null) {
                FileUtils.copyFile(remoteFile, local);
            } else {
                pfd = getActivity().getContentResolver().openFileDescriptor(uri, "r");
                input = new FileInputStream(pfd.getFileDescriptor());
                FileUtils.copyFile(input, local);
            }
        } catch (Throwable e) {
            Log.w("ygo", "copy file " + path + "->" + local.getAbsolutePath(), e);
            return null;
        } finally {
            IOUtils.close(input);
            IOUtils.close(pfd);
        }
        return local;
    }

    private void doUri(Uri uri) {
        Intent startSeting = new Intent(activity, SettingsActivity.class);
        if ("file".equals(uri.getScheme()) || "content".equals(uri.getScheme())) {
            File file = toLocalFile(uri);
            if (file == null || !file.exists()) {
                Toast.makeText(activity, "open file error", Toast.LENGTH_LONG).show();
                return;
            }
            boolean isYdk = file.getName().toLowerCase(Locale.US).endsWith(".ydk");
            boolean isYpk = file.getName().toLowerCase(Locale.US).endsWith(".ypk");
            boolean isYrp = file.getName().toLowerCase(Locale.US).endsWith(".yrp");
            boolean isLua = file.getName().toLowerCase(Locale.US).endsWith(".lua");
            Log.i("ygo", "open file:" + uri + "->" + file.getAbsolutePath());
            if (isYdk) {
                Intent intent = new Intent(getActivity(), DeckManagerActivity.getDeckManager());
                intent.putExtra(Intent.EXTRA_TEXT, file.getAbsolutePath());
                activity.startActivity(intent);
            } else if (isYpk) {
                if (!AppsSettings.get().isReadExpansions()) {
                    activity.startActivity(startSeting);
                    Toast.makeText(activity, R.string.ypk_go_setting, Toast.LENGTH_LONG).show();
                } else {
                    DataManager.get().load(true);
                    Toast.makeText(activity, R.string.ypk_installed, Toast.LENGTH_LONG).show();
                }
            } else if (isYrp) {
                if (!YGOStarter.isGameRunning(getActivity())) {
                    YGOStarter.startGame(getActivity(), null, "-r", file.getName());
                    Toast.makeText(activity, activity.getString(R.string.file_installed), Toast.LENGTH_LONG).show();
                } else {
                    Log.w("ygo", "game is running");
                }
            } else if (isLua) {
                if (!YGOStarter.isGameRunning(getActivity())) {
                    YGOStarter.startGame(getActivity(), null, "-s", file.getName());
                    Toast.makeText(activity, "load single lua file", Toast.LENGTH_LONG).show();
                } else {
                    Log.w("ygo", "game is running");
                }
            }
        } else {
            String host = uri.getHost();
//            if (!Constants.URI_HOST.equalsIgnoreCase(host)) {
//                return;
//            }
            if (Constants.URI_HOST.equals(host)) {
                String name = uri.getQueryParameter(QUERY_NAME);
                if (!TextUtils.isEmpty(name)) {
                    doOpenPath(name);
                } else {
                    Deck deckInfo = new Deck(uri);
                    File file = deckInfo.saveTemp(AppsSettings.get().getDeckDir());
                    Intent startdeck = new Intent(getActivity(), DeckManagerActivity.getDeckManager());
                    startdeck.putExtra(Intent.EXTRA_TEXT, file.getAbsolutePath());
                    activity.startActivity(startdeck);
                }
            }
//            else if (PATH_ROOM.equals(path)) {
//                try {
//                    YGOGameOptions options = new YGOGameOptions();
//                    options.mServerAddr = uri.getQueryParameter(Constants.QUERY_HOST);
//                    options.mUserName = uri.getQueryParameter(Constants.QUERY_USER);
//                    options.mPort = Integer.parseInt(uri.getQueryParameter(Constants.QUERY_PORT));
//                    options.mRoomName = uri.getQueryParameter(Constants.QUERY_ROOM);
//                    YGOStarter.startGame(getActivity(), options, null);
//                } catch (Exception e) {
//                    Toast.makeText(getActivity(), R.string.start_game_error, Toast.LENGTH_SHORT).show();
//                    activity.finish();
//                }
//            }
        }
    }

    private void doOpenPath(String name) {
        File deck = null;
        if (!TextUtils.isEmpty(name)) {
            deck = new File(name);
            if (!deck.exists()) {
                deck = new File(AppsSettings.get().getDeckDir(), name);
            }
        }
        if (deck != null && deck.exists()) {
            Intent startdeck = new Intent(getActivity(), DeckManagerActivity.getDeckManager());
            startdeck.putExtra(Intent.EXTRA_TEXT, deck.getAbsolutePath());
            activity.startActivity(startdeck);
        } else {
            Log.w("kk", "no find " + name);
            activity.finish();
        }
    }
}
