package com.auxesisgroup.auxsecure

import android.graphics.Color
import android.graphics.Typeface.DEFAULT_BOLD
import android.os.Bundle
import android.support.annotation.NonNull
import android.support.v4.content.ContextCompat
import android.support.v4.view.PagerAdapter
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.huanhailiuxin.coolviewpager.CoolViewPager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_actions.*
import kotlinx.android.synthetic.main.toolbar_layout.*
import org.jetbrains.anko.*
import org.jetbrains.anko.design.textInputEditText
import org.jetbrains.anko.design.textInputLayout
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.support.v4.space
import kotlin.properties.Delegates


class ActionsActivity: AppCompatActivity(), ApiCallback {

    private val explorerUrl = "https://testnet.auxledger.org/#/transaction/"

    private val clientId = 100023
    private var contractAddress = ""
    private var contractTxHash = ""
    private var itemUpdate: ItemUpdate = ItemUpdate()
    private var itemCode: String by Delegates.notNull()
    private var isRemoveCall = false
    private var isAddedAlready = false
    private var isUpdateCall = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actions)

        pd = indeterminateProgressDialog("Executing network request..Please wait...")

        itemCode = intent.extras["itemCode"].toString()

        WebService.getItem("$clientId", itemCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { res ->
                            when (res) {
                                is Item -> {
                                    itemUpdate = ItemUpdate(res.name, res.message, res.link, res.details)
                                    when (itemCode) {
                                        res.code -> {
                                            Log.e("isCreated", "TAG is created")
                                            val details: String = try {
                                                res.details[2].content
                                            } catch (ioobe: IndexOutOfBoundsException) {
                                                "..."
                                            }
                                            contractAddress = res.details[0].content
                                            contractTxHash = res.details[1].content
                                            initView()
                                            btnDeploy.visibility = View.VISIBLE
                                            btnUpdateDetails.visibility = View.VISIBLE
                                            btnViewDetails.visibility = View.VISIBLE
                                            tagView.visibility = View.VISIBLE
                                            tagDetails.text = """
                                                Item Code: ${res.code}
                                                Item Name: ${res.name}
                                                Merchant Name: ${res.message}
                                                URL: ${res.link.url}
                                                Details: $details""".trimIndent()
                                        }
                                    }
                                }
                            }
                        },
                        { err ->
                            Log.e("isCreated","TAG is not created")
                            Log.e("isCreated","Attempting to create")
                            dismissProgressBar()
                            WebService.addItemFirst("$clientId",itemCode,
                                    ItemUpdate("...", "...", Link("...", "..."),
                                            listOf(Detail("...","..."))))
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(
                                            { resFirst ->
                                                initView()
                                                Log.d(" Add First Response", resFirst.toJSONLike())
                                            },
                                            { errFirst ->
                                                Log.e("Add First Error", errFirst.toJSONLike())
                                            }
                                    )
                            Log.e("Other Error", err.toJSONLike())
                        }
                )

    }

    private fun initView() {
        dismissProgressBar()
        val views = mutableListOf<View>()

        views.add(0, btnDeployLayout)
        views.add(1, btnUpdateDetailsLayout)
        views.add(2, btnViewDetailsLayout)

        val btnAdapter = CoolAdapter(views)

        cvp.setScrollMode(CoolViewPager.ScrollMode.HORIZONTAL)
        cvp.adapter = btnAdapter

        cvp.addOnPageChangeListener(object : CoolViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(p0: Int) {}

            override fun onPageScrolled(position: Int, p1: Float, p2: Int) {
                when (position) {
                    0 -> {
                        toolbarTitle.text = "Admin View"
                        toolbarTitle.textColor = Color.parseColor("#FF8000")
                        btnDeployLayout.visibility = View.VISIBLE
                        btnUpdateDetailsLayout.visibility = View.GONE
                        btnViewDetailsLayout.visibility = View.GONE
                    }
                    1 -> {
                        toolbarTitle.text = "Merchant View"
                        toolbarTitle.textColor = Color.parseColor("#0000FF")
                        btnDeployLayout.visibility = View.GONE
                        btnUpdateDetailsLayout.visibility = View.VISIBLE
                        btnViewDetailsLayout.visibility = View.GONE
                    }
                    2 -> {
                        toolbarTitle.text = "Customer View"
                        toolbarTitle.textColor = Color.parseColor("#008000")
                        btnDeployLayout.visibility = View.GONE
                        btnUpdateDetailsLayout.visibility = View.GONE
                        btnViewDetailsLayout.visibility = View.VISIBLE
                    }
                }
            }

            override fun onPageSelected(p0: Int) {}

        })

        btnDeploy.onClick {
            showProgressBar()
            isRemoveCall = false
            isUpdateCall = false
            isAddedAlready = false
            WebService.getItemForUpdate("$clientId", itemCode, this@ActionsActivity)
        }

        btnUpdateDetails.onClick {
            alert {
                customView {
                    verticalLayout {
                        padding = dip(20)

                        val name = textInputLayout {
                            hint = "Item Name"
                            textInputEditText()
                        }
                        val merchant = textInputLayout {
                            hint = "Merchant Name"
                            textInputEditText()
                        }

                        val linkUrl = textInputLayout {
                            hint = "URL"
                            textInputEditText()
                        }

                        /*val linkText = textInputLayout {
                            hint = "URL Text"
                            textInputEditText()
                        }*/

                        /*val heading = textInputLayout {
                            hint = "Details Key"
                            textInputEditText()
                        }*/
                        val content = textInputLayout {
                            hint = "Details Value"
                            textInputEditText()
                        }

                        positiveButton("Update Details") {
                            val itemUpdate = ItemUpdate(name.getInput(), merchant.getInput(),
                                    Link(linkUrl.getInput(), ".."),
                                    listOf(Detail("scAddress",contractAddress),
                                            Detail("scTxHash", contractTxHash),
                                            Detail("Details", content.getInput())))

                            Log.e("ITEM UPDATE", itemUpdate.toJSONLike())
                            showProgressBar()
                            WebService.updateItem("$clientId", itemCode, itemUpdate, this@ActionsActivity)
                            isUpdateCall = true
                            setItemDetails(contractAddress, name.getInput(), merchant.getInput(), linkUrl.getInput(), content.getInput())
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(
                                            { res ->
                                                dismissProgressBar()
                                                alert {
                                                    customView {
                                                        title = "Item Details Updated Successfully"
                                                        verticalLayout {
                                                            padding = dip(20)
                                                            textView("Your transaction id is :\n${res.transactionHash}") {
                                                                typeface = DEFAULT_BOLD
                                                                textSize = sp(6).toFloat()
                                                            }
                                                            space().lparams(height = dip(16))

                                                            button("View on Auxledger Blockchain") {
                                                                textSize = sp(8).toFloat()
                                                                textColor = Color.WHITE
                                                                background = ContextCompat.getDrawable(ctx, R.drawable.button_rounded_blue)
                                                                onClick { browse("$explorerUrl${res.transactionHash}") }
                                                            }
                                                        }
                                                    }
                                                    positiveButton("GO BACK") {}
                                                }.show()
                                                Log.d("TxHash", res.transactionHash)
                                            },
                                            { err ->
                                                Log.e("Error", err.toJSONLike())
                                            }
                                    )
                        }
                    }
                }
            }.show()
        }

        btnViewDetails.onClick {
            showProgressBar()
            //TODO("Write function to fetch details from SC")
        }

        if (contractAddress.contains("0x")) {
            btnViewDetailsInitial.visibility = View.VISIBLE
            btnViewDetailsInitial.onClick {
                showProgressBar()
                //TODO("Write function to fetch details from SC")
            }
        }
    }

    override fun <T> onResponse(res: T) {
        when {
            !isRemoveCall -> when (res) {
                is Item -> {

                    itemUpdate = ItemUpdate(res.name, res.message, res.link, res.details)

                    when {
                        itemUpdate.details[0].content.contains("0x") -> {

                            dismissProgressBar()
                            contractAddress = itemUpdate.details[0].content
                            contractTxHash = itemUpdate.details[1].content
                            btnUpdateDetailsLayout.visibility = View.VISIBLE
                            btnViewDetails.visibility = View.VISIBLE

                            isAddedAlready = true

                            when {
                                !isUpdateCall ->
                                    when {
                                        isAddedAlready -> alert {
                                            title = "Already Added to Blockchain"
                                            okButton {  }
                                            negativeButton("Remove contract address") {
                                                isRemoveCall = true
                                                val updatedItemUpdate = itemUpdate.copy(details = listOf(Detail("scAddress", "..."),
                                                        Detail("scTxHash", "..."), Detail("...", "...")))
                                                WebService.updateItem("$clientId", itemCode, updatedItemUpdate, this@ActionsActivity)
                                                showProgressBar()
                                            }
                                        }.show()
                                    }
                                else -> showProgressBar()
                            }

                        }
                        else -> deployAuxSecureSC(clientId.toBigInteger(), itemCode.toBigInteger())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(
                                        { scRes ->
                                            dismissProgressBar()
                                            contractAddress = scRes.contractAddress
                                            contractTxHash = scRes.transactionReceipt.transactionHash

                                            alert {
                                                title = "Added Successfully to Blockchain"
                                                message = "Contract Address : $contractAddress\n\nTxHash : $contractTxHash"
                                                customView {
                                                    verticalLayout {
                                                        padding = dip(20)
                                                        button("View on Auxledger Blockchain") {
                                                            padding = dip(2)
                                                            textSize = sp(8).toFloat()
                                                            textColor = Color.WHITE
                                                            background = ContextCompat.getDrawable(ctx, R.drawable.button_rounded_blue)
                                                            onClick { browse("$explorerUrl$contractTxHash") }
                                                        }
                                                    }
                                                }

                                            }.show()
                                            Log.d("Contract Address", contractAddress)
                                            Log.d("TxHash", scRes.transactionReceipt.transactionHash)
                                            Log.d("TxHash", "${scRes.transactionReceipt}")

                                            val updatedItemUpdate = itemUpdate.copy(details = listOf(Detail("scAddress", contractAddress),
                                                    Detail("scTxHash", contractTxHash)))
                                            WebService.addItem("$clientId", itemCode, updatedItemUpdate, this@ActionsActivity)
                                        },
                                        { err ->
                                            Log.e("Error", err.toJSONLike())
                                        }
                                )
                    }
                }
                else -> alert {
                    title = "Response"
                    message = res.toJSONLike()
                    okButton {  }
                }.show()
            }
            else -> alert {
                dismissProgressBar()
                title = "Removal Successful!"
                okButton {  }
            }.show()
        }
    }

    override fun <T> onError(err: T) {
        toast(err.toString())
    }

    override fun onPause() {
        dismissProgressBar()
        super.onPause()
    }

    internal class CoolAdapter(private val views: List<View>) : PagerAdapter() {

        override fun getCount(): Int {
            return views.size
        }

        override fun isViewFromObject(@NonNull view: View, @NonNull `object`: Any): Boolean {
            return view === `object`
        }

        @NonNull
        override fun instantiateItem(@NonNull container: ViewGroup, position: Int): Any {
            if (views[position].parent != null) {
                (views[position].parent as ViewGroup).removeView(views[position])
            }
            container.addView(views[position])
            return views[position]
        }

        override fun destroyItem(@NonNull container: ViewGroup, position: Int, @NonNull `object`: Any) {
            container.removeView(`object` as View)
        }
    }
}