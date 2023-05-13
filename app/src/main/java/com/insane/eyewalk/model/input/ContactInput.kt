package com.insane.eyewalk.model.input

data class ContactInput(
    var name: String = "",
    var phones: List<PhoneInput> = ArrayList<PhoneInput>(),
    var emails: List<EmailInput> = ArrayList<EmailInput>(),
    var emergency: Boolean = false
) {
}
