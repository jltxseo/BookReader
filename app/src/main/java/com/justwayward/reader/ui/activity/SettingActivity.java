package com.justwayward.reader.ui.activity;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.SwitchCompat;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.justwayward.reader.R;
import com.justwayward.reader.base.BaseActivity;
import com.justwayward.reader.base.Constant;
import com.justwayward.reader.bean.support.RefreshCollectionListEvent;
import com.justwayward.reader.component.AppComponent;
import com.justwayward.reader.component.DaggerMainComponent;
import com.justwayward.reader.manager.CacheManager;
import com.justwayward.reader.manager.SettingManager;
import com.justwayward.reader.utils.SharedPreferencesUtil;

import org.greenrobot.eventbus.EventBus;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by xiaoshu on 2016/10/8.
 */
public class SettingActivity extends BaseActivity {

    public static void startActivity(Context context) {
        context.startActivity(new Intent(context, SettingActivity.class));
    }

    @Bind(R.id.mTvSort)
    TextView mTvSort;
    @Bind(R.id.tvFlipStyle)
    TextView mTvFlipStyle;
    @Bind(R.id.tvCacheSize)
    TextView mTvCacheSize;
    @Bind(R.id.noneCoverCompat)
    SwitchCompat noneCoverCompat;


    @Override
    public int getLayoutId() {
        return R.layout.activity_setting;
    }

    @Override
    protected void setupActivityComponent(AppComponent appComponent) {
        DaggerMainComponent.builder()
                .appComponent(appComponent)
                .build()
                .inject(this);
    }

    @Override
    public void initToolBar() {
        mCommonToolbar.setTitle("设置");
        mCommonToolbar.setNavigationIcon(R.drawable.ab_back);
    }

    @Override
    public void initDatas() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String cachesize = CacheManager.getInstance().getCacheSize();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTvCacheSize.setText(cachesize);
                    }
                });

            }
        }).start();
        mTvSort.setText(getResources().getStringArray(R.array.setting_dialog_sort_choice)[
                SharedPreferencesUtil.getInstance().getBoolean(Constant.ISBYUPDATESORT, true) ? 0 : 1]);
        mTvFlipStyle.setText(getResources().getStringArray(R.array.setting_dialog_style_choice)[
                SharedPreferencesUtil.getInstance().getInt(Constant.FLIP_STYLE, 1)]);
    }


    @Override
    public void configViews() {
        noneCoverCompat.setChecked(SettingManager.getInstance().isNoneCover());
        noneCoverCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SettingManager.getInstance().saveNoneCover(isChecked);
            }
        });
    }

    @OnClick(R.id.bookshelfSort)
    public void onClickBookShelfSort() {
        new AlertDialog.Builder(mContext)
                .setTitle("书架排序方式")
                .setSingleChoiceItems(getResources().getStringArray(R.array.setting_dialog_sort_choice),
                        SharedPreferencesUtil.getInstance().getBoolean(Constant.ISBYUPDATESORT, true) ? 0 : 1,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mTvSort.setText(getResources().getStringArray(R.array.setting_dialog_sort_choice)[which]);
                                SharedPreferencesUtil.getInstance().putBoolean(Constant.ISBYUPDATESORT, which == 0);
                                EventBus.getDefault().post(new RefreshCollectionListEvent());
                                dialog.dismiss();
                            }
                        })
                .create().show();
    }

    @OnClick(R.id.rlFlipStyle)
    public void onClickFlipStyle() {
        new AlertDialog.Builder(mContext)
                .setTitle("阅读页翻页效果")
                .setSingleChoiceItems(getResources().getStringArray(R.array.setting_dialog_style_choice),
                        SharedPreferencesUtil.getInstance().getInt(Constant.FLIP_STYLE, 1),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mTvFlipStyle.setText(getResources().getStringArray(R.array.setting_dialog_style_choice)[which]);
                                SharedPreferencesUtil.getInstance().putInt(Constant.FLIP_STYLE, which);
                                dialog.dismiss();
                            }
                        })
                .create().show();
    }

    @OnClick(R.id.feedBack)
    public void feedBack() {
        FeedbackActivity.startActivity(this);
    }

    @OnClick(R.id.cleanCache)
    public void onClickCleanCache() {
        final boolean selected[] = {true};
        new AlertDialog.Builder(mContext)
                .setTitle("清除缓存")
                .setCancelable(true)
                .setMultiChoiceItems(new String[]{"同时删除阅读记录"}, selected, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        switch (which) {
                            case 0:
                                selected[0] = true;
                                break;
                            default:
                                break;
                        }
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                CacheManager.getInstance().clearCache(selected[0]);
                                final String cacheSize = CacheManager.getInstance().getCacheSize();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mTvCacheSize.setText(cacheSize);
                                        EventBus.getDefault().post(new RefreshCollectionListEvent());
                                    }
                                });
                            }
                        }).start();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create().show();
    }
}
