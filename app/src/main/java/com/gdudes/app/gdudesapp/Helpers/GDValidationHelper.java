package com.gdudes.app.gdudesapp.Helpers;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.gdudes.app.gdudesapp.R;

import java.util.ArrayList;
import java.util.List;

import gdudesapp.GDValidatorLib.Form;
import gdudesapp.GDValidatorLib.Validate;
import gdudesapp.GDValidatorLib.validator.EmailValidator;
import gdudesapp.GDValidatorLib.validator.NotEmptyValidator;
import gdudesapp.GDValidatorLib.validator.RegExpValidator;

public class GDValidationHelper {
    public static final int iNumberOnly = 0;
    public static final int iAlphaNumericOnly = 1;
    public static final int iLeastDigAndLetter = 2;
    public static final int iCharOnly = 3;
    public static final int iEventClubNameValidation = 4;
    public static final int iUserNameValidation = 5;
    public static final int iPasswordValidation = 6;
    public static final int iAlphaNumericStartWithLetter = 7;
    public static final int iTagDescValidation = 8;
    public static final int iAddressValidation = 9;
    public static final int iEmailValidation = 10;

    private static final String VNumberOnly = "Only numbers allowed";
    private static final String VAlphaNumericOnly = "Only alphanumeric characters allowed";
    private static final String VCharRange = "Number of characters should be between %d and %d";
    private static final String VLeastDigAndLetter = "Should contain at least one digit and one letter";
    private static final String VCharOnly = "Only characters allowed";
    private static final String VEventClubNameValidation = "Should start with a letter and can contain letters, numbers and undersocre ( _ ) and must end with a letter or number";
    private static final String VUserNameValidation = "Username should be between 3 to 20 characters long.";
    private static final String VPasswordValidation = "Should contain at least one digit and one letter. Can contain characters from ! @ # $ % ^ & *";
    private static final String VAlphaNumericStartWithLetter = "Should be alphanumeric and start with a letter";
    private static final String VTagDescValidation = "Should start with a letter and can contain letters and numbers. Can contain characters from _ . , & ' ? \" ! - @";
    private static final String VAddressValidation = "Can contain letters and numbers. Can contain characters from _ . , & ' ? \" ! - @";
    private static final String VEmailValidation = "Invalid Email address";


    private List<EditText> mValidationElements;
    private List<Validate> mValidations;
    private Context mContext;
    private Form mValidateForm;
    private Boolean HasBeenValidatedOnce = false;
    private TextWatcher EditTextWatcher;

    public GDValidationHelper(Context context, List<EditText> ValidationElements) {
        try {
            mContext = context;
            mValidationElements = ValidationElements;
            mValidateForm = new Form();
            mValidations = new ArrayList<>();
            for (int i = 0; i < mValidationElements.size(); i++) {
                mValidations.add(new Validate(mValidationElements.get(i)));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    public void RemoveValidators(int position) {
        try {
            if (mValidations.size() < (position + 1)) {
                return;
            }
            mValidations.get(position).RemoveAll();
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    public GDValidationHelper AddNonEmptyValidator(int position) {
        try {
            mValidations.get(position).addValidator(new NotEmptyValidator(mContext, R.string.required_field_validation_message));
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
        return this;
    }

    public GDValidationHelper AddRegexValidator(int position, int RegexType) {
        try {
            RegExpValidator regExpValidator = new RegExpValidator(mContext, getRegexPatternErrorMessage(RegexType));
            regExpValidator.setPattern(getRegexPattern(RegexType));
            mValidations.get(position).addValidator(regExpValidator);
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
        return this;
    }

    public GDValidationHelper AddCharRangeValidator(int position, int minimumCharacters, int maximumCharacters) {
        try {
            RegExpValidator regExpValidator = new RegExpValidator(mContext, getCharRangeRegexPatternErrorMessage(minimumCharacters, maximumCharacters));
            regExpValidator.setPattern(getCharRangeRegexPattern(minimumCharacters, maximumCharacters));
            mValidations.get(position).addValidator(regExpValidator);
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
        return this;
    }

    public GDValidationHelper AddEmailValidator(int position) {
        try {
            EmailValidator emailValidator = new EmailValidator(mContext, R.string.invalid_email);
            mValidations.get(position).addValidator(emailValidator);
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
        return this;
    }

    public void UpdateFormValidators() {
        try {
            for (int i = 0; i < mValidations.size(); i++) {
                mValidateForm.addValidates(mValidations.get(i));
            }
            EditTextWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (HasBeenValidatedOnce) {
                        ValidateInternal();
                    }
                }
            };
            for (int i = 0; i < mValidationElements.size(); i++) {
                mValidationElements.get(i).addTextChangedListener(EditTextWatcher);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
    }

    public Boolean Validate() {
        try {
            for (int i = 0; i < mValidationElements.size(); i++) {
                mValidationElements.get(i).setText(mValidationElements.get(i).getText().toString().trim());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
        }
        return ValidateInternal();
    }

    private Boolean ValidateInternal() {
        try {
            HasBeenValidatedOnce = true;
            return mValidateForm.validate();
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            return false;
        }
    }

    public int GetFirstErrorPosition() {
        try {
            return mValidateForm.GetFirstErrorPosition();
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            return 0;
        }
    }

    private String getRegexPattern(final int RegexType) {
        String RegexPattern = "";
        switch (RegexType) {
            case iNumberOnly:
                RegexPattern = "^[0-9]*$";
                break;
            case iAlphaNumericOnly:
                RegexPattern = "^[a-zA-Z0-9]*$";
                break;
            case iLeastDigAndLetter:
                RegexPattern = "^[a-zA-Z0-9]*$";
                break;
            case iCharOnly:
                RegexPattern = "^[a-zA-Z]*$";
                break;
            case iEventClubNameValidation:
                RegexPattern = "^[a-zA-Z][a-zA-Z0-9 _]*[a-zA-Z0-9]$";
                break;
            case iUserNameValidation:
                RegexPattern = "\\w{3,20}\\b";
                break;
            case iPasswordValidation:
                RegexPattern = "^(?=.*\\d)(?=.*[a-zA-Z]).[A-Za-z0-9!@#$%^&*]*$";
                break;
            case iAlphaNumericStartWithLetter:
                RegexPattern = "^[a-zA-Z].[a-zA-Z0-9 ]*$";
                break;
            case iTagDescValidation:
                RegexPattern = "^[a-zA-Z][a-zA-Z0-9 \n_.,'&?\"!-@]*$";
                break;
            case iAddressValidation:
                RegexPattern = "^[a-zA-Z0-9 \n_.,'&?\"!-@]*$";
                break;
            default:
                RegexPattern = "";
                break;
        }
        return RegexPattern;
    }

    private String getRegexPatternErrorMessage(final int RegexType) {
        String RegexPatternErrorMessage = "";
        switch (RegexType) {
            case iNumberOnly:
                RegexPatternErrorMessage = VNumberOnly;
                break;
            case iAlphaNumericOnly:
                RegexPatternErrorMessage = VAlphaNumericOnly;
                break;
            case iLeastDigAndLetter:
                RegexPatternErrorMessage = VLeastDigAndLetter;
                break;
            case iCharOnly:
                RegexPatternErrorMessage = VCharOnly;
                break;
            case iEventClubNameValidation:
                RegexPatternErrorMessage = VEventClubNameValidation;
                break;
            case iUserNameValidation:
                RegexPatternErrorMessage = VUserNameValidation;
                break;
            case iPasswordValidation:
                RegexPatternErrorMessage = VPasswordValidation;
                break;
            case iAlphaNumericStartWithLetter:
                RegexPatternErrorMessage = VAlphaNumericStartWithLetter;
                break;
            case iTagDescValidation:
                RegexPatternErrorMessage = VTagDescValidation;
                break;
            case iAddressValidation:
                RegexPatternErrorMessage = VAddressValidation;
                break;
//            case iEmailValidation:
//                RegexPatternErrorMessage = VEmailValidation;
//                break;
            default:
                RegexPatternErrorMessage = "Invalid Input";
                break;
        }
        return RegexPatternErrorMessage;
    }

    private String getCharRangeRegexPattern(int minimumCharacters, int maximumCharacters) {
        try {
            return "^\\s*(?:\\S\\s*){" + Integer.toString(minimumCharacters) + "," + Integer.toString(maximumCharacters) + "}$";
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            return "";
        }
    }

    private String getCharRangeRegexPatternErrorMessage(int minimumCharacters, int maximumCharacters) {
        try {
            return String.format(VCharRange, new Object[]{minimumCharacters, maximumCharacters});
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            return "";
        }
    }
}
