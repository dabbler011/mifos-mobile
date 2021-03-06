package org.mifos.mobilebanking.ui.fragments;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.mifos.mobilebanking.R;
import org.mifos.mobilebanking.models.beneficary.Beneficiary;
import org.mifos.mobilebanking.models.payload.TransferPayload;
import org.mifos.mobilebanking.models.templates.account.AccountOption;
import org.mifos.mobilebanking.models.templates.account.AccountOptionsTemplate;
import org.mifos.mobilebanking.presenters.ThirdPartyTransferPresenter;
import org.mifos.mobilebanking.ui.activities.base.BaseActivity;
import org.mifos.mobilebanking.ui.enums.TransferType;
import org.mifos.mobilebanking.ui.fragments.base.BaseFragment;
import org.mifos.mobilebanking.ui.views.ThirdPartyTransferView;
import org.mifos.mobilebanking.utils.Constants;
import org.mifos.mobilebanking.utils.DateHelper;
import org.mifos.mobilebanking.utils.MFDatePicker;
import org.mifos.mobilebanking.utils.ProcessView;
import org.mifos.mobilebanking.utils.Toaster;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by dilpreet on 21/6/17.
 */

public class ThirdPartyTransferFragment extends BaseFragment implements ThirdPartyTransferView,
        AdapterView.OnItemSelectedListener {

    @BindView(R.id.sp_beneficiary)
    Spinner spBeneficiary;

    @BindView(R.id.sp_pay_from)
    Spinner spPayFrom;

    @BindView(R.id.et_amount)
    EditText etAmount;

    @BindView(R.id.et_remark)
    EditText etRemark;

    @BindView(R.id.ll_make_transfer)
    LinearLayout layoutMakeTransfer;

    @BindView(R.id.process_one)
    ProcessView pvOne;

    @BindView(R.id.process_two)
    ProcessView pvTwo;

    @BindView(R.id.process_three)
    ProcessView pvThree;

    @BindView(R.id.process_four)
    ProcessView pvFour;

    @BindView(R.id.btn_pay_to)
    AppCompatButton btnPayTo;

    @BindView(R.id.btn_pay_from)
    AppCompatButton btnPayFrom;

    @BindView(R.id.btn_amount)
    AppCompatButton btnAmount;

    @BindView(R.id.ll_review)
    LinearLayout llReview;

    @BindView(R.id.tv_select_beneficary)
    TextView tvSelectBeneficary;

    @BindView(R.id.tv_select_amount)
    TextView tvEnterAmount;

    @BindView(R.id.tv_enter_remark)
    TextView tvEnterRemark;

    @Inject
    ThirdPartyTransferPresenter presenter;

    private List<String> listBeneficiary = new ArrayList<>();
    private List<String> listPayFrom = new ArrayList<>();
    private List<Beneficiary> beneficiaries;
    private ArrayAdapter<String> beneficiaryAdapter;
    private ArrayAdapter<String> payFromAdapter;
    private AccountOption fromAccountOption;
    private AccountOption beneficiaryAccountOption;
    private AccountOptionsTemplate accountOptionsTemplate;
    private String transferDate;
    private View rootView;

    public static ThirdPartyTransferFragment newInstance() {
        ThirdPartyTransferFragment fragment = new ThirdPartyTransferFragment();
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ((BaseActivity) getActivity()).getActivityComponent().inject(this);
        rootView = inflater.inflate(R.layout.fragment_third_party_transfer, container, false);
        setToolbarTitle(getString(R.string.third_party_transfer));
        ButterKnife.bind(this, rootView);

        showUserInterface();

        presenter.attachView(this);
        if (savedInstanceState == null) {
            presenter.loadTransferTemplate();
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Constants.TEMPLATE, accountOptionsTemplate);
        outState.putParcelableArrayList(Constants.BENEFICIARY, new ArrayList<Parcelable>(
                beneficiaries));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            showThirdPartyTransferTemplate((AccountOptionsTemplate) savedInstanceState.
                    getParcelable(Constants.TEMPLATE));
            List<Beneficiary> tempBeneficiaries = savedInstanceState.getParcelableArrayList(
                    Constants.BENEFICIARY);
            showBeneficiaryList(tempBeneficiaries);
        }
    }

    /**
     * Setting up basic components
     */
    @Override
    public void showUserInterface() {
        payFromAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item,
                listPayFrom);
        payFromAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spPayFrom.setAdapter(payFromAdapter);
        spPayFrom.setOnItemSelectedListener(this);

        beneficiaryAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item,
                listBeneficiary);
        beneficiaryAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        spBeneficiary.setAdapter(beneficiaryAdapter);
        spBeneficiary.setOnItemSelectedListener(this);

        transferDate = DateHelper.getSpecificFormat(DateHelper.FORMAT_dd_MMMM_yyyy,
                MFDatePicker.getDatePickedAsString());

        pvOne.setCurrentActive();
    }

    /**
     * Checks validation of {@code etRemark} and then opens {@link TransferProcessFragment} for
     * initiating the transfer
     */
    @OnClick(R.id.btn_review_transfer)
    public void reviewTransfer() {
        if (etAmount.getText().toString().equals("")) {
            Toaster.show(rootView, getString(R.string.enter_amount));
            return;
        }

        if (etAmount.getText().toString().equals(".")) {
            Toaster.show(rootView, getString(R.string.invalid_amount));
            return;
        }

        if (etRemark.getText().toString().equals("")) {
            Toaster.show(rootView, getString(R.string.remark_is_mandatory));
            return;
        }

        if (spBeneficiary.getSelectedItem().toString().
                equals(spPayFrom.getSelectedItem().toString())) {
            Toaster.show(rootView, getString(R.string.error_same_account_transfer));
            return;
        }

        TransferPayload transferPayload = new TransferPayload();
        transferPayload.setFromAccountId(fromAccountOption.getAccountId());
        transferPayload.setFromClientId(fromAccountOption.getClientId());
        transferPayload.setFromAccountType(fromAccountOption.getAccountType().getId());
        transferPayload.setFromOfficeId(fromAccountOption.getOfficeId());
        transferPayload.setToOfficeId(beneficiaryAccountOption.getOfficeId());
        transferPayload.setToAccountId(beneficiaryAccountOption.getAccountId());
        transferPayload.setToClientId(beneficiaryAccountOption.getClientId());
        transferPayload.setToAccountType(beneficiaryAccountOption.getAccountType().getId());
        transferPayload.setTransferDate(transferDate);
        transferPayload.setTransferAmount(Double.parseDouble(etAmount.getText().toString()));
        transferPayload.setTransferDescription(etRemark.getText().toString());

        ((BaseActivity) getActivity()).replaceFragment(TransferProcessFragment.
                newInstance(transferPayload, TransferType.TPT), true, R.id.container);

    }

    /**
     * Shows a {@link android.support.design.widget.Snackbar} with {@code message}
     * @param msg String to be shown
     */
    @Override
    public void showToaster(String msg) {
        Toaster.show(rootView, msg);
    }

    /**
     * Provides with {@code accountOptionsTemplate} fetched from server which is used to update
     * {@code listPayFrom}
     * @param accountOptionsTemplate Template for account transfer
     */
    @Override
    public void showThirdPartyTransferTemplate(AccountOptionsTemplate accountOptionsTemplate) {
        this.accountOptionsTemplate = accountOptionsTemplate;
        listPayFrom.addAll(presenter.getAccountNumbersFromAccountOptions(accountOptionsTemplate.
                getFromAccountOptions()));
        payFromAdapter.notifyDataSetChanged();

    }

    /**
     * Provides with {@code beneficiaries} fetched from server which is used to update
     * {@code listBeneficiary}
     * @param beneficiaries List of {@link Beneficiary} linked with user's account
     */
    @Override
    public void showBeneficiaryList(List<Beneficiary> beneficiaries) {
        this.beneficiaries = beneficiaries;
        listBeneficiary.addAll(presenter.getAccountNumbersFromBeneficiaries(beneficiaries));
        beneficiaryAdapter.notifyDataSetChanged();
    }


    /**
     *  Disables {@code spPayFrom} {@link Spinner} and sets {@code pvOne} to completed and make
     * {@code pvThree} pvTwo
     */
    @OnClick(R.id.btn_pay_from)
    public void payFromSelected() {
        pvOne.setCurrentCompeleted();
        pvTwo.setCurrentActive();

        btnPayFrom.setVisibility(View.GONE);
        tvSelectBeneficary.setVisibility(View.GONE);
        btnPayTo.setVisibility(View.VISIBLE);
        spBeneficiary.setVisibility(View.VISIBLE);
        spPayFrom.setEnabled(false);
    }


    /**
     * Checks validation of {@code spBeneficiary} {@link Spinner}.<br>
     *  Disables {@code spBeneficiary} {@link Spinner} and sets {@code pvTwo} to completed and make
     * {@code pvThree} active
     */
    @OnClick(R.id.btn_pay_to)
    public void payToSelected() {
        if (spBeneficiary.getSelectedItem().toString().equals(spPayFrom.getSelectedItem().
                toString())) {
            showToaster(getString(R.string.error_same_account_transfer));
            return;
        }
        pvTwo.setCurrentCompeleted();
        pvThree.setCurrentActive();

        btnPayTo.setVisibility(View.GONE);
        tvEnterAmount.setVisibility(View.GONE);
        etAmount.setVisibility(View.VISIBLE);
        btnAmount.setVisibility(View.VISIBLE);
        spBeneficiary.setEnabled(false);
    }

    /**
     * Checks validation of {@code etAmount} {@link EditText}.<br>
     * Disables {@code etAmount} and sets {@code pvThree} to completed and make
     * {@code pvFour} active
     */
    @OnClick(R.id.btn_amount)
    public void amountSet() {

        if (etAmount.getText().toString().equals("")) {
            showToaster(getString(R.string.enter_amount));
            return;
        }

        if (etAmount.getText().toString().equals(".")) {
            showToaster(getString(R.string.invalid_amount));
            return;
        }

        if (etAmount.getText().toString().matches("^0*")) {
            showToaster(getString(R.string.amount_greater_than_zero));
            return;
        }

        pvThree.setCurrentCompeleted();
        pvFour.setCurrentActive();

        btnAmount.setVisibility(View.GONE);
        tvEnterRemark.setVisibility(View.GONE);
        etRemark.setVisibility(View.VISIBLE);
        llReview.setVisibility(View.VISIBLE);
        etAmount.setEnabled(false);
    }

    /**
     * It is called whenever any error occurs while executing a request
     * @param msg Error message that tells the user about the problem.
     */
    @Override
    public void showError(String msg) {
        Toaster.show(rootView, msg);
    }

    @Override
    public void showProgress() {
        layoutMakeTransfer.setVisibility(View.GONE);
        showProgressBar();
    }

    @Override
    public void hideProgress() {
        hideProgressBar();
        layoutMakeTransfer.setVisibility(View.VISIBLE);
    }

    /**
     * Callback for {@code spPayFrom} and {@code spBeneficiary}
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.sp_beneficiary:
                beneficiaryAccountOption = presenter.searchAccount(accountOptionsTemplate.
                        getFromAccountOptions(), beneficiaryAdapter.getItem(position));
                break;
            case R.id.sp_pay_from:
                fromAccountOption = accountOptionsTemplate.getFromAccountOptions().get(position);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.detachView();
    }
}
