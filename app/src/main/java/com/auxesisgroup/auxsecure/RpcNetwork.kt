package com.auxesisgroup.auxsecure

import android.os.Environment
import org.web3j.crypto.Credentials
import org.web3j.crypto.WalletUtils
import org.web3j.protocol.Web3j
import org.web3j.protocol.Web3jFactory
import org.web3j.protocol.http.HttpService
import java.io.File

// TODO("Add Auxledger Node Url to BuildConfig")
private var auxLedgerNodeUrl = ""

private var web3j = Web3jFactory.build(HttpService(auxLedgerNodeUrl))

private var gasPrice: Long = 21000000000
private var gasLimit: Long = 4712388

// TODO("Add Private Key to BuildConfig")
//private var privateKey = BuildConfig.PRIVATEKEY
//private var metamaskCreds = Credentials.create(privateKey)

fun getWeb3J(): Web3j = web3j

var appdir = "${Environment.getExternalStorageDirectory()}/genuinety/wallet"

fun createNewWallet(pass: String) : String {
    return WalletUtils.generateLightNewWalletFile(pass, File(appdir))
}

fun loadCredentials(pass: String) : Credentials{
    return WalletUtils.loadCredentials(pass, File(appdir).listFiles().first())
}