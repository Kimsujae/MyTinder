package kr.ac.kumoh.s20160250.mytinder

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth
        callbackManager = CallbackManager.Factory.create()



        initLoginButton()
        initSignUpButton()
        initEmailAndPasswordEditText()
        initFacebookLoginButton()
    }

    private fun initLoginButton() {
        val loginButton = findViewById<Button>(R.id.loginButton)
        loginButton.setOnClickListener {
            val email = getInputEmail()
            val password = getInputPassword()

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        handleSuccessLogin()
                    } else {
                        Toast.makeText(
                            this,
                            "로그인에 실패하였습니다. 이메일또는 비밀번호를 확인하여주세요",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun initSignUpButton() {
        val signUpButton = findViewById<Button>(R.id.signUpButton)
        signUpButton.setOnClickListener {
            val email = getInputEmail()
            val password = getInputPassword()
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this,
                            "회원가입에 성공했습니다. 로그인 버튼을 눌러 로그인 해주세요",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(this, "이미 가입한 이메일이거나, 회원가입에 실패했습니다.", Toast.LENGTH_SHORT)
                            .show()
                    }

                }

        }
    }

    private fun initEmailAndPasswordEditText() {
        val emailEditText = findViewById<EditText>(R.id.passwordEditText)
        val passwordEditText = findViewById<EditText>(R.id.emailEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val signUpButton = findViewById<Button>(R.id.signUpButton)

        emailEditText.addTextChangedListener {
            val enable = emailEditText.text.isNotEmpty() and passwordEditText.text.isNotEmpty()
            loginButton.isEnabled = enable
            signUpButton.isEnabled = enable
        }
        passwordEditText.addTextChangedListener {
            val enable = emailEditText.text.isNotEmpty() and passwordEditText.text.isNotEmpty()
            loginButton.isEnabled = enable
            signUpButton.isEnabled = enable
        }
    }

    private fun initFacebookLoginButton() {
        val facebookLoginButton = findViewById<LoginButton>(R.id.facebookLoginButton)

        facebookLoginButton.setPermissions("email", "public_profile")
        facebookLoginButton.registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onCancel() {}

                override fun onError(error: FacebookException) {
                    Log.d("LoginActivity", error.toString())
                    Toast.makeText(this@LoginActivity, "페이스북 로그인이 실패하였습니다.", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onSuccess(result: LoginResult) {
                    val credential = FacebookAuthProvider.getCredential(result.accessToken.token)
                    auth?.signInWithCredential(credential)
                        ?.addOnCompleteListener(this@LoginActivity) { task ->
                            Log.d("LoginActivity", task.toString())
                            if (task.isSuccessful) {
                                handleSuccessLogin()
                            } else {
                                Log.d("LoginActivity", result.toString())
                                Toast.makeText(
                                    this@LoginActivity,
                                    "페이스북 로그인이 실패하였습니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
            })
    }

    private fun getInputPassword() = findViewById<EditText>(R.id.passwordEditText).text.toString()

    private fun getInputEmail() = findViewById<EditText>(R.id.emailEditText).text.toString()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun handleSuccessLogin() {
        if(auth.currentUser ==null){
            Toast.makeText(this,"로그인에 실패하였습니다.",Toast.LENGTH_SHORT).show()
            return
        }
        val userId = auth.currentUser?.uid
        val currentUserDB = Firebase.database.reference.child("Users").child(userId.orEmpty())
        val user = mutableMapOf<String, Any>()
        user["userId"] = userId.orEmpty()
        currentUserDB.updateChildren(user)
        finish()

    }
}