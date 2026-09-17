package com.sleeplessdog.banquerito.ui.icons

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import banquerito.shared.generated.resources.Res
import banquerito.shared.generated.resources.*
import org.jetbrains.compose.resources.painterResource

object AppIcons {
    @Composable fun chatSend(): Painter = painterResource(Res.drawable.ic_chat_send)
    @Composable fun chatAttach(): Painter = painterResource(Res.drawable.ic_chat_attach)
    @Composable fun chatStt(): Painter = painterResource(Res.drawable.ic_chat_stt)
    @Composable fun wallet(): Painter = painterResource(Res.drawable.ic_wallet)
    @Composable fun transfer(): Painter = painterResource(Res.drawable.ic_transfers)
    @Composable fun strategy(): Painter = painterResource(Res.drawable.ic_strategy)
    @Composable fun account(): Painter = painterResource(Res.drawable.ic_account)
    @Composable fun payments(): Painter = painterResource(Res.drawable.ic_payments)
    @Composable fun income(): Painter = painterResource(Res.drawable.ic_income)
    @Composable fun payment(): Painter = painterResource(Res.drawable.ic_payment)
    @Composable fun bank(): Painter = painterResource(Res.drawable.ic_bank)
    @Composable fun agent(): Painter = painterResource(Res.drawable.ic_agent)
    @Composable fun settings(): Painter = painterResource(Res.drawable.ic_settings)
    @Composable fun arrowBack(): Painter = painterResource(Res.drawable.icon_arrow_leg_left)
    @Composable fun info(): Painter = painterResource(Res.drawable.icon_info)
    @Composable fun backspace(): Painter = painterResource(Res.drawable.icon_delete_large)
    @Composable fun done(): Painter = painterResource(Res.drawable.icon_send)
    @Composable fun minus(): Painter = painterResource(Res.drawable.icon_minus_small)
    @Composable fun plus(): Painter  = painterResource(Res.drawable.icon_plus_small)
}