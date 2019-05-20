package com.asahina.test.ui

import android.databinding.DataBindingUtil
import android.databinding.ObservableArrayList
import android.databinding.ObservableList
import android.os.Bundle
import android.support.v7.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem
import com.asahina.test.BR
import com.asahina.test.R
import com.asahina.test.databinding.ActivityMainBinding
import com.asahina.test.item.ReminderItemList
import com.asahina.test.view.recyclerView.RecyclerViewBindingUtils

import kotlinx.android.synthetic.main.activity_main.*
import android.content.DialogInterface
import android.widget.EditText
import android.app.AlertDialog
import android.support.v4.util.Consumer
import android.widget.Toast
import com.asahina.test.preference.Preferences


class MainActivity : AppCompatActivity() {

    val itemList: ObservableList<ReminderItemList> = ObservableArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.view = this

        itemList.addAll(Preferences().loadReminderList())

//        RecyclerViewBindingUtilsは朝比奈さんの特製ライブラリ
        RecyclerViewBindingUtils.bindSortable(
            binding.taskList,
            itemList,
            BR.viewModel,
            R.layout.item_reminder,
            R.id.sort,
            Consumer {
//                トースターの表示
//                Toast.makeText(this, it.label, 0).show()

                // テキスト入力用Viewの作成
//                val editView = EditText(this@MainActivity)
//                editView.setText(it.label.get())
//
//                AlertDialog.Builder(this@MainActivity)
//                    .setTitle("編集")
//                    .setView(editView)
//                    // OKボタンの設定
//                    .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, whichButton ->
//                        // OKボタンをタップした時の処理をここに記述
//                        it.label.set(editView.text.toString())
////                        Preferences().saveReminderList(itemList)
//                    })
//                    // キャンセルボタンの設定
//                    .setNegativeButton("キャンセル", DialogInterface.OnClickListener { dialog, whichButton ->
//                        // キャンセルボタンをタップした時の処理をここに記述
//                    })
//                    .show()

//                チェック
                it.check.set(!it.check.get())
            },
            Consumer {

                // テキスト入力用Viewの作成
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("削除")
                    .setMessage("本当にさくじょしますか")
                    // OKボタンの設定
                    .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, whichButton ->
                        // OKボタンをタップした時の処理をここに記述
                       itemList.remove(it)
                    })
                    // キャンセルボタンの設定
                    .setNegativeButton("キャンセル", DialogInterface.OnClickListener { dialog, whichButton ->
                        // キャンセルボタンをタップした時の処理をここに記述
                    })
                    .show()

            })



        setSupportActionBar(toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun selectAddItem() {
        // テキスト入力用Viewの作成
        val editView = EditText(this@MainActivity)

        val dialog = AlertDialog.Builder(this@MainActivity)

        dialog.setTitle("テキストを入力してください")
        dialog.setView(editView)

// OKボタンの設定
        dialog.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, whichButton ->
            // OKボタンをタップした時の処理をここに記述
            itemList.add(ReminderItemList(editView.text.toString()))
//            Preferences().saveReminderList(itemList)
        })

// キャンセルボタンの設定
        dialog.setNegativeButton("キャンセル", DialogInterface.OnClickListener { dialog, whichButton ->
            // キャンセルボタンをタップした時の処理をここに記述
        })

        dialog.show()

    }

    override fun onStop() {
        super.onStop()
        Preferences().saveReminderList(itemList)
    }
}
