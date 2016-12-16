package com.anbaoxing.autoble4_0;

import android.content.Context;
import android.widget.Toast;

/**
 *吐司---》多次点击也仅显示LENGTH_SHORT时长
 * 不会一直显示
 * Created by LENOVO on 2016/9/19.
 */
public class ToastUtil {
    private static Toast toast;
    public static void showToast(Context context, String string){
        if (toast == null){
            toast = Toast.makeText(context,string,Toast.LENGTH_SHORT);
        }else {
            toast.setText(string);
        }
        toast.show();
    }
}
