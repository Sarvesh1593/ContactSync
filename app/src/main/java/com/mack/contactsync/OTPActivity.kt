package com.mack.contactsync

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.mack.contactsync.databinding.ActivityOtpactivityBinding
import org.w3c.dom.Text
import java.util.concurrent.TimeUnit

class OTPActivity : AppCompatActivity() {
    private lateinit var binding : ActivityOtpactivityBinding
    private lateinit var verificationBtn : Button
    private lateinit var resendTV : TextView
    private lateinit var inputOTP1 : EditText
    private lateinit var inputOTP2 : EditText
    private lateinit var inputOTP3 : EditText
    private lateinit var inputOTP4 : EditText
    private lateinit var inputOTP5 : EditText
    private lateinit var inputOTP6 : EditText
    private lateinit var auth : FirebaseAuth
    private lateinit var number: String
    private lateinit var resendToken : PhoneAuthProvider.ForceResendingToken
    private lateinit var OTP : String
    private lateinit var progressbar : ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        OTP = intent.getStringExtra("verificationId").toString()
        resendToken = intent.getParcelableExtra("resendToken")!!
        number = intent.getStringExtra("phone_Number")!!
        init()
        progressbar.visibility = View.INVISIBLE
        addTextChangeListener()
        resendOTPVisibility()
        resendTV.setOnClickListener {
            resendVerificationCode()
            resendOTPVisibility()
        }
        verificationBtn.setOnClickListener {
            val typedOTP = (inputOTP1.text.toString() + inputOTP2.text.toString() + inputOTP3.text.toString() +
                    inputOTP4.text.toString() + inputOTP5.text.toString() + inputOTP6.text.toString())
            if(typedOTP.isNotEmpty()){
                if(typedOTP.length == 6){
                    val credential : PhoneAuthCredential = PhoneAuthProvider.getCredential(
                        OTP, typedOTP
                    )
                    progressbar.visibility = View.VISIBLE
                    signInWithPhoneAuthCredential(credential)
                }else{
                    Toast.makeText(this,"Please Enter correct OTP",Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this,"Please Enter OTP",Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun resendOTPVisibility(){
        inputOTP1.setText("")
        inputOTP2.setText("")
        inputOTP3.setText("")
        inputOTP4.setText("")
        inputOTP5.setText("")
        inputOTP6.setText("")
        resendTV.visibility = View.INVISIBLE
        resendTV.isEnabled = false
        Handler(Looper.myLooper()!!).postDelayed({
            resendTV.visibility = View.VISIBLE
            resendTV.isEnabled = true
        },40000)
    }
    private fun resendVerificationCode(){
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(number) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .setForceResendingToken(resendToken)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }
    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            signInWithPhoneAuthCredential(credential)
        }


        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
            } else if (e is FirebaseAuthMissingActivityForRecaptchaException) {
                // reCAPTCHA verification attempted with null Activity
            }

            // Show a message and update the UI
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken,
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            OTP = verificationId
            resendToken = token
        }
    }
    private fun sendToMain(){
        val intent = Intent(this,MainActivity::class.java)
        intent.putExtra("phone_number",number)
        startActivity(intent)
    }
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    progressbar.visibility = View.INVISIBLE
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(this,"Authentication is Successfully",Toast.LENGTH_SHORT).show()
                    sendToMain()
                } else {
                    // Sign in failed, display a message and update the UI
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Log.d("TAG",task.exception.toString())
                    }
                }
            }
    }
    private fun addTextChangeListener(){
        inputOTP1.addTextChangedListener(EditTextWatcher(inputOTP1))
        inputOTP2.addTextChangedListener(EditTextWatcher(inputOTP2))
        inputOTP3.addTextChangedListener(EditTextWatcher(inputOTP3))
        inputOTP4.addTextChangedListener(EditTextWatcher(inputOTP4))
        inputOTP5.addTextChangedListener(EditTextWatcher(inputOTP5))
        inputOTP6.addTextChangedListener(EditTextWatcher(inputOTP6))
    }
    private fun init(){
        auth = FirebaseAuth.getInstance()
        verificationBtn = binding.submitOtp
        progressbar = binding.progressBar
        resendTV = binding.resendOtp
        inputOTP1 = binding.editOtp1
        inputOTP2 = binding.editOtp2
        inputOTP3 = binding.editOtp3
        inputOTP4 = binding.editOtp4
        inputOTP5 = binding.editOtp5
        inputOTP6 = binding.editOtp6
    }
    inner class EditTextWatcher(private val view : View) :  TextWatcher{
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable?) {

            val text = s.toString()
            when(view.id){
                R.id.edit_otp_1 -> if(text.length ==1) inputOTP2.requestFocus()
                R.id.edit_otp_2 -> if(text.length ==1) inputOTP3.requestFocus() else if(text.isEmpty()) inputOTP1.requestFocus()
                R.id.edit_otp_3 -> if(text.length ==1) inputOTP4.requestFocus() else if(text.isEmpty()) inputOTP2.requestFocus()
                R.id.edit_otp_4 -> if(text.length ==1) inputOTP5.requestFocus() else if(text.isEmpty()) inputOTP3.requestFocus()
                R.id.edit_otp_5 -> if(text.length ==1) inputOTP6.requestFocus() else if(text.isEmpty()) inputOTP4.requestFocus()
                R.id.edit_otp_6 -> if(text.isEmpty()) inputOTP5.requestFocus()
            }
        }

    }
}