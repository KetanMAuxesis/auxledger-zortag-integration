package com.auxesisgroup.auxsecure

import android.os.Environment
import io.reactivex.Observable
import org.web3j.crypto.Credentials
import org.web3j.crypto.WalletUtils
import org.web3j.protocol.Web3j
import org.web3j.protocol.Web3jFactory
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.http.HttpService
import org.web3j.tuples.generated.Tuple6
import java.io.File
import java.math.BigInteger

private var web3j = Web3jFactory.build(HttpService(BuildConfig.AUXLEDGERNODEURL))

private var gasPrice: Long = 21000000000
private var gasLimit: Long = 4712388

private var privateKey = BuildConfig.PRIVATEKEY
private var metamaskCreds = Credentials.create(privateKey)

fun getWeb3J(): Web3j = web3j

var appdir = "${Environment.getExternalStorageDirectory()}/genuinety/wallet"

fun createNewWallet(pass: String) : String {
    return WalletUtils.generateLightNewWalletFile(pass, File(appdir))
}

fun loadCredentials(pass: String) : Credentials{
    return WalletUtils.loadCredentials(pass, File(appdir).listFiles().first())
}

fun deployAuxSecureSC(clientId: BigInteger, itemCode: BigInteger) : Observable<AuxSecureSC> {
    showProgressBar()
    return AuxSecureSC.deploy(getWeb3J(), metamaskCreds, gasPrice.toBigInteger(), gasLimit.toBigInteger(), clientId, itemCode).newObservable()
}

fun setItemDetails(contractAddress: String, itemName: String, merchantName: String, link: String, details: String) : Observable<TransactionReceipt> {
    showProgressBar()
    return AuxSecureSC.load(contractAddress, getWeb3J(), metamaskCreds, gasPrice.toBigInteger(), gasLimit.toBigInteger())
            .setItemDetails(itemName, merchantName, link, details).newObservable()
}

fun getItemDetails(contractAddress: String) : Observable<Tuple6<BigInteger, BigInteger, String, String, String, String>> {
    showProgressBar()
    return AuxSecureSC.load(contractAddress, getWeb3J(), metamaskCreds, gasPrice.toBigInteger(), gasLimit.toBigInteger())
            .itemDetails.newObservable()
}