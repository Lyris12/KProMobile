package cn.garymb.ygomobile.ui.home;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tubb.smrv.SwipeHorizontalMenuLayout;

import cn.garymb.ygomobile.lite.R;
import cn.garymb.ygomobile.ui.adapters.BaseRecyclerAdapterPlus;

public class ServerInfoViewHolder extends BaseRecyclerAdapterPlus.BaseViewHolder {
    public ServerInfoViewHolder(View itemView) {
        super(itemView);
        serverName = $(R.id.server_name);
        serverDesc = $(R.id.server_desc);
        serverIp = $(R.id.text_ip);
        serverPort = $(R.id.text_port);
        userName = $(R.id.text_player);
        btnEdit = $(R.id.btn_edit_delete);
        btnDelete = $(R.id.btn_delete);
        mMenuLayout = $(R.id.swipe_layout);
        contentView = $(R.id.smContentView);
        iv_fond=$(R.id.iv_fond);
    }

    public final SwipeHorizontalMenuLayout mMenuLayout;
    public final View contentView;
    public final TextView serverName;
    public final TextView serverDesc;
    public final TextView userName;
    public final TextView serverIp;
    public final TextView serverPort;
    public final View btnEdit, btnDelete;
    public ImageView iv_fond;
}
