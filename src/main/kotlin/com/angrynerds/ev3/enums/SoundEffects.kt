package com.angrynerds.ev3.enums

import com.angrynerds.ev3.extensions.getRandomElement

enum class SoundEffects(val fileName: String) {
    BEAR("bear.wav"),
    CRACK_KID("crack_kid.wav"),
    DONKEY("donkey.wav"),
    ERROR("error.wav"),
    MLG_HORNS("mlg_horns.wav"),
    OVER_9000("over_9000.wav"),
    SUPER_MARIO_COIN("super_mario_coin.wav"),
    YHAC("yhac.wav");

    companion object {
        fun getRandomSuccessSound(): SoundEffects {
            return arrayListOf(SoundEffects.CRACK_KID, SoundEffects.MLG_HORNS,
                    SoundEffects.OVER_9000, SUPER_MARIO_COIN).getRandomElement()
        }
    }
}