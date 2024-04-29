package io.horizontalsystems.bankwallet.modules.sendevmtransaction

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.horizontalsystems.bankwallet.R
import io.horizontalsystems.bankwallet.core.ethereum.CautionViewItem
import io.horizontalsystems.bankwallet.core.iconPlaceholder
import io.horizontalsystems.bankwallet.core.imageUrl
import io.horizontalsystems.bankwallet.core.shorten
import io.horizontalsystems.bankwallet.core.slideFromBottom
import io.horizontalsystems.bankwallet.modules.evmfee.Cautions
import io.horizontalsystems.bankwallet.modules.evmfee.EvmFeeCellViewModel
import io.horizontalsystems.bankwallet.modules.evmfee.FeeSettingsInfoDialog
import io.horizontalsystems.bankwallet.modules.fee.FeeCell
import io.horizontalsystems.bankwallet.modules.multiswap.QuoteInfoRow
import io.horizontalsystems.bankwallet.modules.multiswap.ui.DataField
import io.horizontalsystems.bankwallet.modules.send.SendModule
import io.horizontalsystems.bankwallet.modules.send.evm.settings.SendEvmNonceViewModel
import io.horizontalsystems.bankwallet.ui.compose.ComposeAppTheme
import io.horizontalsystems.bankwallet.ui.compose.components.ButtonSecondaryDefault
import io.horizontalsystems.bankwallet.ui.compose.components.CellUniversalLawrenceSection
import io.horizontalsystems.bankwallet.ui.compose.components.CoinImage
import io.horizontalsystems.bankwallet.ui.compose.components.HFillSpacer
import io.horizontalsystems.bankwallet.ui.compose.components.HSpacer
import io.horizontalsystems.bankwallet.ui.compose.components.NftIcon
import io.horizontalsystems.bankwallet.ui.compose.components.RowUniversal
import io.horizontalsystems.bankwallet.ui.compose.components.TransactionInfoAddressCell
import io.horizontalsystems.bankwallet.ui.compose.components.TransactionInfoContactCell
import io.horizontalsystems.bankwallet.ui.compose.components.VSpacer
import io.horizontalsystems.bankwallet.ui.compose.components.caption_grey
import io.horizontalsystems.bankwallet.ui.compose.components.cell.SectionUniversalLawrence
import io.horizontalsystems.bankwallet.ui.compose.components.headline2_leah
import io.horizontalsystems.bankwallet.ui.compose.components.subhead1_grey
import io.horizontalsystems.bankwallet.ui.compose.components.subhead1_leah
import io.horizontalsystems.bankwallet.ui.compose.components.subhead2_grey
import io.horizontalsystems.bankwallet.ui.compose.components.subhead2_leah
import io.horizontalsystems.bankwallet.ui.helpers.TextHelper
import io.horizontalsystems.core.helpers.HudHelper
import io.horizontalsystems.marketkit.models.Blockchain
import io.horizontalsystems.marketkit.models.BlockchainType
import io.horizontalsystems.marketkit.models.Coin
import io.horizontalsystems.marketkit.models.Token
import io.horizontalsystems.marketkit.models.TokenType

@Composable
fun SendEvmTransactionViewNew(
    navController: NavController,
    items: List<SectionViewItem>,
    cautions: List<CautionViewItem>,
    transactionFields: List<DataField>,
    networkFee: SendModule.AmountData?,
) {
    Column {
        items.forEach { sectionViewItem ->
            SectionView(sectionViewItem.viewItems, navController)
        }

        if (transactionFields.isNotEmpty()) {
            VSpacer(height = 16.dp)
            SectionUniversalLawrence {
                transactionFields.forEachIndexed { index, field ->
                    field.GetContent(navController, index != 0)
                }
            }
        }

        VSpacer(height = 16.dp)
        SectionUniversalLawrence {
            QuoteInfoRow(
                title = {
                    val title = stringResource(id = R.string.FeeSettings_NetworkFee)
                    val infoText = stringResource(id = R.string.FeeSettings_NetworkFee_Info)

                    subhead2_grey(text = title)

                    Image(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .clickable(
                                onClick = {
                                    navController.slideFromBottom(
                                        R.id.feeSettingsInfoDialog,
                                        FeeSettingsInfoDialog.Input(title, infoText)
                                    )
                                },
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            )
                        ,
                        painter = painterResource(id = R.drawable.ic_info_20),
                        contentDescription = ""
                    )

                },
                value = {
                    val primary = networkFee?.primary?.getFormattedPlain() ?: "---"
                    val secondary = networkFee?.secondary?.getFormattedPlain() ?: "---"

                    Column(horizontalAlignment = Alignment.End) {
                        subhead2_leah(text = primary)
                        VSpacer(height = 1.dp)
                        subhead2_grey(text = secondary)
                    }
                }
            )
        }

        if (cautions.isNotEmpty()) {
            Cautions(cautions)
        }
    }
}

@Composable
fun SendEvmTransactionView(
    feeCellViewModel: EvmFeeCellViewModel,
    nonceViewModel: SendEvmNonceViewModel,
    navController: NavController,
    items: List<SectionViewItem>,
    cautions: List<CautionViewItem>?,
) {
    val fee by feeCellViewModel.feeLiveData.observeAsState(null)
    val viewState by feeCellViewModel.viewStateLiveData.observeAsState()

    Column {
        items.forEach { sectionViewItem ->
            SectionView(sectionViewItem.viewItems, navController)
        }

        NonceView(nonceViewModel)

        Spacer(Modifier.height(16.dp))
        CellUniversalLawrenceSection(
            listOf {
                FeeCell(
                    title = stringResource(R.string.FeeSettings_NetworkFee),
                    info = stringResource(R.string.FeeSettings_NetworkFee_Info),
                    value = fee,
                    viewState = viewState,
                    navController = navController
                )
            }
        )

        cautions?.let {
            Cautions(it)
        }
    }
}

@Composable
private fun NonceView(nonceViewModel: SendEvmNonceViewModel) {
    val uiState = nonceViewModel.uiState
    if (!uiState.showInConfirmation) return
    val nonce = uiState.nonce ?: return

    Spacer(Modifier.height(16.dp))
    CellUniversalLawrenceSection(
        listOf {
            RowUniversal(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                subhead2_grey(
                    text = stringResource(id = R.string.Send_Confirmation_Nonce)
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = nonce.toString(),
                    maxLines = 1,
                    style = ComposeAppTheme.typography.subhead1,
                    color = setColorByType(ValueType.Regular)
                )
            }
        }
    )
}

@Composable
private fun SectionView(viewItems: List<ViewItem>, navController: NavController) {
    Spacer(Modifier.height(16.dp))
    CellUniversalLawrenceSection(viewItems) { item ->
        when (item) {
            is ViewItem.Subhead -> Subhead(item)
            is ViewItem.Value -> TitleValue(item)
            is ViewItem.ValueMulti -> TitleValueMulti(item)
            is ViewItem.AmountMulti -> AmountMulti(item)
            is ViewItem.Amount -> Amount(item)
            is ViewItem.AmountWithTitle -> AmountWithTitle(item)
            is ViewItem.NftAmount -> NftAmount(item)
            is ViewItem.Address -> TransactionInfoAddressCell(item.title, item.value, item.showAdd, item.blockchainType, navController)
            is ViewItem.ContactItem -> TransactionInfoContactCell(item.contact.name)
            is ViewItem.Input -> TitleValueHex("Input", item.value.shorten(), item.value)
            is ViewItem.TokenItem -> Token(item)
        }
    }
}

@Composable
private fun Subhead(item: ViewItem.Subhead) {
    RowUniversal(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        item.iconRes?.let {
            Icon(
                modifier = Modifier.padding(end = 16.dp),
                painter = painterResource(id = it),
                contentDescription = null,
                tint = ComposeAppTheme.colors.grey
            )
        }
        headline2_leah(
            text = item.title
        )
        Spacer(Modifier.weight(1f))
        subhead1_grey(
            text = item.value
        )
    }
}

@Composable
fun TitleValue(item: ViewItem.Value) {
    RowUniversal(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        subhead2_grey(
            text = item.title
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = item.value,
            maxLines = 1,
            style = ComposeAppTheme.typography.subhead1,
            color = setColorByType(item.type)
        )
    }
}

@Composable
private fun TitleValueMulti(item: ViewItem.ValueMulti) {
    RowUniversal(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        subhead2_grey(
            text = item.title
        )
        Spacer(Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = item.primaryValue,
                maxLines = 1,
                style = ComposeAppTheme.typography.subhead1,
                color = setColorByType(item.type)
            )
            Text(
                text = item.secondaryValue,
                maxLines = 1,
                style = ComposeAppTheme.typography.caption,
                color = ComposeAppTheme.colors.grey
            )
        }
    }
}

@Composable
private fun AmountMulti(item: ViewItem.AmountMulti) {
    RowUniversal(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        CoinImage(
            modifier = Modifier.size(32.dp),
            iconUrl = item.token.coin.imageUrl,
            placeholder = item.token.iconPlaceholder
        )
        Column(
            modifier = Modifier
                .padding(start = 16.dp)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.amounts[0].coinAmount,
                    maxLines = 1,
                    style = ComposeAppTheme.typography.subhead1,
                    color = setColorByType(item.type)
                )
                Spacer(Modifier.weight(1f))
                subhead2_grey(
                    text = item.amounts[0].fiatAmount ?: ""
                )
            }
            if (item.amounts.size > 1) {
                Spacer(Modifier.height(3.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    caption_grey(
                        text = item.amounts[1].coinAmount
                    )
                    Spacer(Modifier.weight(1f))
                    caption_grey(
                        text = item.amounts[1].fiatAmount ?: ""
                    )
                }
            }
        }
    }
}

@Composable
private fun Amount(item: ViewItem.Amount) {
    RowUniversal(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        CoinImage(
            modifier = Modifier
                .padding(end = 16.dp)
                .size(32.dp),
            iconUrl = item.token.coin.imageUrl,
            placeholder = item.token.iconPlaceholder
        )
        Text(
            text = item.coinAmount,
            maxLines = 1,
            style = ComposeAppTheme.typography.subhead1,
            color = setColorByType(item.type)
        )
        Spacer(Modifier.weight(1f))
        subhead2_grey(
            text = item.fiatAmount ?: ""
        )
    }
}

@Composable
private fun AmountWithTitle(item: ViewItem.AmountWithTitle) {
    RowUniversal(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        CoinImage(
            modifier = Modifier.size(32.dp),
            iconUrl = item.token.coin.imageUrl,
            placeholder = item.token.iconPlaceholder
        )
        HSpacer(16.dp)
        Column {
            subhead2_leah(text = item.title)
            VSpacer(height = 1.dp)
            caption_grey(text = item.badge ?: stringResource(id =R.string.CoinPlatforms_Native))
        }
        HFillSpacer(minWidth = 8.dp)
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = item.coinAmount,
                maxLines = 1,
                style = ComposeAppTheme.typography.subhead1,
                color = setColorByType(item.type)
            )
            item.fiatAmount?.let {
                VSpacer(height = 1.dp)
                subhead2_grey(text = it)
            }
        }
    }
}

@Composable
private fun NftAmount(item: ViewItem.NftAmount) {
    RowUniversal(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        NftIcon(
            modifier = Modifier.padding(end = 16.dp),
            iconUrl = item.iconUrl,
        )
        Text(
            text = item.amount,
            maxLines = 1,
            style = ComposeAppTheme.typography.subhead2,
            color = setColorByType(item.type)
        )
    }
}

@Composable
private fun Token(item: ViewItem.TokenItem) {
    RowUniversal(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        CoinImage(
            modifier = Modifier
                .padding(end = 16.dp)
                .size(32.dp),
            iconUrl = item.token.coin.imageUrl,
            placeholder = item.token.iconPlaceholder
        )
        subhead1_leah(item.token.coin.code)
    }
}

@Composable
private fun TitleValueHex(
    title: String,
    valueTitle: String,
    value: String,
) {
    val localView = LocalView.current
    RowUniversal(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        subhead2_grey(
            text = title
        )
        Spacer(Modifier.weight(1f))
        ButtonSecondaryDefault(
            modifier = Modifier.height(28.dp),
            title = valueTitle,
            onClick = {
                TextHelper.copyText(value)
                HudHelper.showSuccessMessage(localView, R.string.Hud_Text_Copied)
            }
        )
    }
}

@Composable
private fun setColorByType(type: ValueType) =
    when (type) {
        ValueType.Regular -> ComposeAppTheme.colors.bran
        ValueType.Disabled -> ComposeAppTheme.colors.grey
        ValueType.Outgoing -> ComposeAppTheme.colors.leah
        ValueType.Incoming -> ComposeAppTheme.colors.remus
        ValueType.Warning -> ComposeAppTheme.colors.jacob
        ValueType.Forbidden -> ComposeAppTheme.colors.lucian
    }

@Preview
@Composable
private fun Preview_Subhead() {
    val item = ViewItem.Subhead("Title", "Value", R.drawable.ic_arrow_down_left_24)
    ComposeAppTheme {
        Subhead(item)
    }
}

@Preview
@Composable
private fun Preview_TitleValue() {
    val item = ViewItem.Value("Title", "Value", ValueType.Incoming)
    ComposeAppTheme {
        TitleValue(item)
    }
}

@Preview
@Composable
private fun Preview_AmountMulti() {
    val token = Token(
        coin = Coin("uid", "KuCoin", "KCS"),
        blockchain = Blockchain(BlockchainType.Ethereum, "Ethereum", null),
        type = TokenType.Eip20("eef"),
        decimals = 18
    )
    val item = ViewItem.AmountMulti(
        listOf(
            AmountValues("0.104 KCS (est)", "$0.99"),
            AmountValues("0.103 KCS (min)", "$0.95"),
        ),
        ValueType.Incoming,
        token
    )
    ComposeAppTheme {
        AmountMulti(item)
    }
}

@Preview
@Composable
private fun Preview_Amount() {
    val token = Token(
        coin = Coin("uid", "KuCoin", "KCS"),
        blockchain = Blockchain(BlockchainType.Ethereum, "Ethereum", null),
        type = TokenType.Eip20("eef"),
        decimals = 18
    )
    val item = ViewItem.Amount(
        "$0.99",
        "0.104 KCS (est)",
        ValueType.Outgoing,
        token
    )
    ComposeAppTheme {
        Amount(item)
    }
}

@Preview
@Composable
private fun Preview_TitleValueHex() {
    ComposeAppTheme {
        TitleValueHex("Title", "ValueShort", "ValueLong")
    }
}
