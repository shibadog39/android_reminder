package com.asahina.test.item

import android.databinding.ObservableBoolean
import android.databinding.ObservableField

class ReminderItemList {
    constructor(label: String){
        this.label.set(label)
    }

    val label: ObservableField<String> = ObservableField()
    val check:ObservableBoolean = ObservableBoolean(false)
}