package io.horizontalsystems.bankwallet.modules.receivemain

import android.os.Parcelable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import io.horizontalsystems.bankwallet.R
import io.horizontalsystems.bankwallet.core.BaseComposeFragment
import io.horizontalsystems.bankwallet.core.description
import io.horizontalsystems.bankwallet.core.getInput
import io.horizontalsystems.bankwallet.core.imageUrl
import io.horizontalsystems.bankwallet.core.slideFromRight
import io.horizontalsystems.bankwallet.entities.Account
import io.horizontalsystems.bankwallet.ui.compose.ComposeAppTheme
import io.horizontalsystems.bankwallet.ui.compose.components.AppBar
import io.horizontalsystems.bankwallet.ui.compose.components.CellUniversalLawrenceSection
import io.horizontalsystems.bankwallet.ui.compose.components.HsBackButton
import io.horizontalsystems.bankwallet.ui.compose.components.InfoText
import io.horizontalsystems.bankwallet.ui.compose.components.RowUniversal
import io.horizontalsystems.bankwallet.ui.compose.components.SectionUniversalItem
import io.horizontalsystems.bankwallet.ui.compose.components.VSpacer
import io.horizontalsystems.bankwallet.ui.compose.components.body_leah
import io.horizontalsystems.bankwallet.ui.compose.components.subhead2_grey
import io.horizontalsystems.core.helpers.HudHelper
import io.horizontalsystems.marketkit.models.FullCoin
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

class NetworkSelectFragment : BaseComposeFragment() {

    @Composable
    override fun GetContent(navController: NavController) {
        val input = navController.getInput<Input>()
        if (input == null) {
            HudHelper.showErrorMessage(LocalView.current, R.string.Error_ParameterNotSet)
            navController.popBackStack()
        } else {
            val initViewModel = viewModel(initializer = {
                NetworkSelectInitViewModel(input.coinUid)
            })

            val activeAccount = initViewModel.activeAccount
            val fullCoin = initViewModel.fullCoin

            if (activeAccount != null && fullCoin != null) {
                NetworkSelectScreen(navController, input.popupDestinationId, activeAccount, fullCoin)
            } else {
                HudHelper.showErrorMessage(LocalView.current, "Active account and/or full coin is null")
                navController.popBackStack()
            }
        }
    }

    @Parcelize
    data class Input(val coinUid: String, val popupDestinationId: Int?) : Parcelable
}

@Composable
fun NetworkSelectScreen(
    navController: NavController,
    popupDestinationId: Int?,
    activeAccount: Account,
    fullCoin: FullCoin,
) {
    val viewModel = viewModel<NetworkSelectViewModel>(factory = NetworkSelectViewModel.Factory(activeAccount, fullCoin))
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        backgroundColor = ComposeAppTheme.colors.tyler,
        topBar = {
            AppBar(
                title = stringResource(R.string.Balance_Network),
                navigationIcon = {
                    HsBackButton(onClick = { navController.popBackStack() })
                },
                menuItems = listOf()
            )
        }
    ) {
        Column(Modifier.padding(it)) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                InfoText(
                    text = stringResource(R.string.Balance_NetworkSelectDescription)
                )
                VSpacer(20.dp)
                CellUniversalLawrenceSection(viewModel.eligibleTokens) { token ->
                    val blockchain = token.blockchain
                    SectionUniversalItem {
                        NetworkCell(
                            title = blockchain.name,
                            subtitle = blockchain.description,
                            imageUrl = blockchain.type.imageUrl,
                            onClick = {
                                coroutineScope.launch {
                                    navController.slideFromRight(
                                        R.id.receiveFragment,
                                        viewModel.getOrCreateWallet(token)
                                    )
                                }
                            }
                        )
                    }
                }
                VSpacer(32.dp)
            }
        }
    }
}

@Composable
fun NetworkCell(
    title: String,
    subtitle: String,
    imageUrl: String,
    onClick: (() -> Unit)? = null
) {
    RowUniversal(
        onClick = onClick
    ) {
        Image(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .size(32.dp),
            painter = rememberAsyncImagePainter(
                model = imageUrl,
                error = painterResource(R.drawable.ic_platform_placeholder_32)
            ),
            contentDescription = null,
        )
        Column(modifier = Modifier.weight(1f)) {
            body_leah(text = title)
            subhead2_grey(text = subtitle)
        }
        Icon(
            modifier = Modifier.padding(horizontal = 16.dp),
            painter = painterResource(id = R.drawable.ic_arrow_right),
            contentDescription = null,
            tint = ComposeAppTheme.colors.grey
        )
    }
}
