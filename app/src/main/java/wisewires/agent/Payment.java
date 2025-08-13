package wisewires.agent;

import java.util.List;
import java.util.Map;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Payment {
    static Logger logger = LoggerFactory.getLogger(Payment.class);

    static Map<String, List<String>> MODE_LOCATORS = Map.of(
            "credit card", List.of(".payment-image.adyenCc"));

    static String PAYMENT_FORM_LOCATOR = """
            app-card-on-delivery-payment,
            app-cod-payment,
            app-payment-lacaixa,
            app-payment-mode-2cp2p,
            app-payment-mode-ae-installment,
            app-payment-mode-addi-pay,
            app-payment-mode-adyen-alipay,
            app-payment-mode-adyen-bancontact,
            app-payment-mode-adyen-ideal,
            app-payment-mode-adyen-klarna,
            app-payment-mode-adyen-klarna-installment,
            app-payment-mode-adyen-paypal,
            app-payment-mode-adyen-swish,
            app-payment-mode-affirm,
            app-payment-mode-afterpay,
            app-payment-mode-afterpay-installment,
            app-payment-mode-alfa-installments,
            app-payment-mode-bank-transfer,
            app-payment-mode-billie-b2b,
            app-payment-mode-bizum,
            app-payment-mode-brokerage-installment,
            app-payment-mode-buynow-paylater,
            app-payment-mode-checkout-dot-com,
            app-payment-mode-cmi-credit-card,
            app-payment-mode-commerz,
            app-payment-mode-credit-card,
            app-payment-mode-credit-guradcc,
            app-payment-mode-cybersource-credit-card,
            app-payment-mode-cybersource-credit-card-hk,
            app-payment-mode-cybersource-credit-installment,
            app-payment-mode-cofidis,
            app-payment-mode-dvedo-installments,
            app-payment-mode-eip-installment,
            app-payment-mode-ewallet-napasa,
            app-payment-mode-fatourati,
            app-payment-mode-finance-now,
            app-payment-mode-finance-now-purple-visa,
            app-payment-mode-findomestic,
            app-payment-mode-flexi-card,
            app-payment-mode-float,
            app-payment-mode-floa-nx,
            app-payment-mode-floa3x4x,
            app-payment-mode-flow-khipu,
            app-payment-mode-flow-mach,
            app-payment-mode-flow-webpay,
            app-payment-mode-flow-yape,
            app-payment-mode-flowonepay,
            app-payment-mode-glow,
            app-payment-mode-google-pay,
            app-payment-mode-gmo-docomo,
            app-payment-mode-gmo-paypay,
            app-payment-mode-gopay,
            app-payment-mode-halyke-pay,
            app-payment-mode-halyk-epay-samsung-pay,
            app-payment-mode-hcid,
            app-payment-mode-heidi-pay,
            app-payment-mode-homecredit-installment,
            app-payment-mode-inbank,
            app-payment-mode-indodana,
            app-payment-mode-installment,
            app-payment-mode-installment-mor,
            app-payment-mode-ipay88,
            app-payment-mode-ipay88-ewallet,
            app-payment-mode-ipay88-installment,
            app-payment-mode-ipay88-sop,
            app-payment-mode-iris,
            app-payment-mode-iyzico-bank,
            app-payment-mode-iyzico-credit,
            app-payment-mode-iyzico-wallet,
            app-payment-mode-jp-gmo,
            app-payment-mode-kaspipay,
            app-payment-mode-kbank-credit-card,
            app-payment-mode-klarna,
            app-payment-mode-klarna-de,
            app-payment-mode-kredivo,
            app-payment-mode-kueski-pay,
            app-payment-mode-latitude,
            app-payment-mode-masterpass,
            app-payment-mode-mbway,
            app-payment-mode-mercado-cash,
            app-payment-mode-mercado-checkout-bnpl,
            app-payment-mode-mercado-checkout-pro,
            app-payment-mode-mercado-pago-credit,
            app-payment-mode-mercadopagopse,
            app-payment-mode-midtrans-credit-card-inst,
            app-payment-mode-midtrans-credit-debit-card,
            app-payment-mode-mobile-pay,
            app-payment-mode-moyasar-samsung-pay,
            app-payment-mode-multibanco,
            app-payment-mode-neweb-pay,
            app-payment-mode-ngenius-samsung-pay,
            app-payment-mode-oney,
            app-payment-mode-p24-blik,
            app-payment-mode-p24-fast-bank-transfer,
            app-payment-mode-paidy,
            app-payment-mode-pago-efectivo,
            app-payment-mode-pay-just-now,
            app-payment-mode-payfortcreditcard,
            app-payment-mode-paygate,
            app-payment-mode-paymob,
            app-payment-mode-paydollar,
            app-payment-mode-paypal,
            app-payment-mode-paypal-credit,
            app-payment-mode-payu-google-pay,
            app-payment-mode-payu-installment,
            app-payment-mode-pl-santander,
            app-payment-mode-platon-payment-mode,
            app-payment-mode-pointspay,
            app-payment-mode-razer-installment,
            app-payment-mode-ro-pay-utraditional-bank,
            app-payment-mode-safetypay,
            app-payment-mode-samsung-pay,
            app-payment-mode-santander,
            app-payment-mode-simple-pay,
            app-payment-mode-simpaisa-ewallet,
            app-payment-mode-tabby-pay,
            app-payment-mode-tamara,
            app-payment-mode-tbi-bank,
            app-payment-mode-tbi-bnpl,
            app-payment-mode-tbi-credit-online,
            app-payment-mode-twint,
            app-payment-mode-twoc2p-bnpl-wallets,
            app-payment-mode-twoc2p-card-based-installment,
            app-payment-mode-vipps,
            app-payment-mode-wafacash-pay,
            app-payment-mode-walley-b2b,
            app-payment-mode-wechat-pay,
            app-payment-mode-windcave,
            app-payment-mode-zippay,
            app-privat-bank,
            app-ws-pay,
            corvus-pay,
            paypo-payment,
            payment-mode-cofidis-installment,
            payment-mode-credit-card-installment,
            payment-mode-cybersource-paypal""";

    static String PAY_NOW_LOCATOR = """
            [data-an-la="payment:pay now:card or debit card"]""";

    static void expandPaymentMethod(String methodName) throws Exception {
        String to = "app-payment-modes mat-expansion-panel-header:has(.payment-title)";
        List<String> modeLocators = MODE_LOCATORS.get(methodName.toLowerCase());
        String selector = modeLocators != null ? String.join(",", modeLocators) : "";
        WebUI.waitElement(to, 15);
        WebElement elm = (WebElement) WebUI.driver.executeScript("""
                let elms = document.querySelectorAll(arguments[0]);
                return Array.from(elms).find(e => {
                    if (e.querySelector('.payment-title').innerText.trim().toLowerCase() == arguments[1]) return true;
                    try {
                        if (arguments[2] && e.querySelector(arguments[2])) return true;
                    } catch (e) {}
                    return false;
                })""",
                to, methodName.toLowerCase(), selector);
        if (elm == null)
            throw new Exception("Payment method '%s' not found".formatted(methodName));
        WebUI.scrollToCenter(elm);
        WebUI.delay(1);
        if (elm.getDomAttribute("aria-expanded").equals("true")) {
            logger.info("Payment method '%s' already expanded".formatted(methodName));
        } else {
            WebUI.click(elm);
            logger.info("Payment method '%s' expanded".formatted(methodName));
        }
    }

    public static void acceptTermAndConditions() throws Exception {
        try {
            String to = """
                    app-payment-modes-terms-and-conditions .mdc-checkbox,
                    app-tr-payment-modes-terms-and-conditions-v2 .mdc-checkbox""";
            for (WebElement elm : WebUI.waitElements(to, 5)) {
                Form.check(elm);
            }
            logger.info("All term and conditions accepted");
        } catch (Exception e) {
            throw new Exception("Unable to accept payment Terms and Conditions", e);
        }
    }

    static boolean isPayNowButtonEnable(WebElement elm) {
        WebElement btn = elm.findElement(By.cssSelector(PAY_NOW_LOCATOR));
        return !Form.isDisabled(btn);
    }

    static void clickPayNow() throws Exception {
        try {
            WebElement elm = WebUI.waitElement(PAY_NOW_LOCATOR, 10);
            WebUI.scrollToCenter(elm);
            WebUI.delay(2);
            WebUI.click(elm);
            logger.info("Pay Now button clicked");
        } catch (Exception e) {
            throw new Exception("Unable to click Pay Now button", e);
        }
    }

    static void process(Context c) throws Exception {
        WebElement form = WebUI.waitElement(PAYMENT_FORM_LOCATOR, 15);
        WebUI.waitForNotPresent(".paymentmode-detail-loading", 15);
        switch (form.getTagName()) {
            case
                    "app-card-on-delivery-payment",
                    "app-cod-payment":
                payWithCOD(c, form);
                break;

            case
                    "app-payment-mode-credit-card":
                payWithCreditCard(c, form);
                break;

            case
                    "app-payment-mode-adyen-paypal",
                    "app-payment-mode-paypal",
                    "app-payment-mode-paypal-credit",
                    "payment-mode-cybersource-paypal":
                payWithPayPal(c, form);
                break;

            case
                    "app-payment-mode-glow":
                payWithGlow(c, form);
                break;

            case
                    "app-payment-mode-adyen-klarna",
                    "app-payment-mode-adyen-klarna-installment",
                    "app-payment-mode-klarna",
                    "app-payment-mode-klarna-de":
                payWithKlarna(c, form);
                break;

            default:
                throw new Exception("Unknow payment mode " + form.getTagName());
        }
        logger.info("Payment process success");
    }

    static void payWithCOD(Context c, WebElement elm) throws Exception {

    }

    private static void handleCreditCardField(WebElement field, Context c) throws Exception {
        String nameOrId = WebUI.getDomAttribute(field, "name", "formcontrolname", "id", "placeholder", "type");
        switch (nameOrId) {
            case
                    "cardNumber",
                    "cardnumber",
                    "card-number",
                    "ccNumber",
                    "CCNo",
                    "cc_cardNo",
                    "card_num",
                    "encryptedCardNumber":
                String cardNumber = "4111 1111 1111 1111";
                field.clear();
                field.sendKeys(cardNumber);
                logger.info("Card number entered: " + cardNumber);
                break;
            case
                    "holderNameInput",
                    "input-checkout__cardholderName",
                    "cardHolderName",
                    "cardName",
                    "card-name",
                    "ccName",
                    "CCName",
                    "cc_holdername",
                    "cardholder-name":
                String name = "ABC";
                field.clear();
                field.sendKeys(name);
                logger.info("Holder name entered: " + name);
                break;
            case
                    "encryptedExpiryDate",
                    "expirationDate",
                    "exp-date",
                    "cardExpiry",
                    "expiryDate",
                    "card-expiry",
                    "CCExpdate",
                    "MM ／ YY",
                    "card-expiry-date":
                String expiryDate = "03/30";
                field.clear();
                field.sendKeys(expiryDate);
                logger.info("Expiry date entered: " + expiryDate);
                break;
            case
                    "mm",
                    "cardExpMonth",
                    "month",
                    "ccOpMonth",
                    "expire-m",
                    "card_exp_month",
                    "ccmonth":
                String expiryMonth = "expiryMonth";
                if (field.getTagName().equals("input")) {
                    field.clear();
                    field.sendKeys(expiryMonth);
                } else {
                    Form.select(field, expiryMonth);
                }
                logger.info("Expiry month entered: " + expiryMonth);
                break;
            case
                    "yy",
                    "cardExpYear",
                    "year",
                    "ccOpYear",
                    "expire-y",
                    "card_exp_year",
                    "ccyear":
                String expiryYear = "expiryYear";
                if (field.getTagName().equals("input")) {
                    field.clear();
                    field.sendKeys(expiryYear);
                } else {
                    Form.select(field, expiryYear);
                }
                logger.info("Expiry year entered: " + expiryYear);
                break;
            case
                    "encryptedSecurityCode",
                    "securityCode",
                    "cvc",
                    "cardCVV",
                    "cardCvv",
                    "card-cvv",
                    "cvv",
                    "ccCvv",
                    "CVV",
                    "cc_cvv",
                    "cvv2":
                String cvv = "737";
                field.clear();
                field.sendKeys(cvv);
                field.sendKeys(Keys.TAB);
                logger.info("CVV code entered: " + cvv);
                break;
        }
    }

    static void payWithCreditCard(Context c, WebElement form) throws Exception {
        String IN_PAGE_LOCATOR = "iframe, input:not([type='hidden'])";
        String IN_FRAME_LOCATOR = "input:not([type='hidden']):not([aria-hidden])";

        WebUI.scrollToCenter(form);
        WebUI.wait(300, 1).withMessage("form filled").until(d -> {
            try {
                List<WebElement> elms = form.findElements(By.cssSelector(IN_PAGE_LOCATOR));
                for (WebElement elm : elms) {
                    if (!elm.isDisplayed() || Form.isProcessed(elm))
                        continue;
                    String tagName = elm.getTagName();
                    if (tagName.equals("iframe")) {
                        try {
                            WebUI.driver.switchTo().frame(elm);
                            List<WebElement> inputs = WebUI.findElements(IN_FRAME_LOCATOR);
                            for (WebElement input : inputs) {
                                handleCreditCardField(input, c);
                            }
                            WebUI.driver.switchTo().defaultContent();
                        } catch (Exception ignore) {
                            WebUI.driver.switchTo().defaultContent();
                        }
                    } else {
                        handleCreditCardField(elm, c);
                    }
                }
                acceptTermAndConditions();
                return isPayNowButtonEnable(form);
            } catch (Exception ignore) {
                return false;
            }
        });
        logger.info("Pay Now button is enabled");

        clickPayNow();

        WebUI.wait(30).withMessage("order confirmation").until(d -> {
            if (d.getCurrentUrl().contains("/orderConfirmation"))
                return true;

            return false;
        });
    }

    static void payWithPayPal(Context c, WebElement elm) throws Exception {

    }

    static void payWithGlow(Context c, WebElement elm) throws Exception {

    }

    static void payWithKlarna(Context c, WebElement elm) throws Exception {

    }
}
