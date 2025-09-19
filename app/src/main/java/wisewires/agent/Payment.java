package wisewires.agent;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Payment {
    static Logger logger = LoggerFactory.getLogger(Payment.class);

    static List<String> MODE_CC = List.of(
            ".payment-image.adyenCc",
            ".payment-image.kbank",
            ".payment-image.mercadopago");

    static Map<String, List<String>> MODE_LOCATORS = Map.ofEntries(
            Map.entry("3ds card", MODE_CC),
            Map.entry("amex card", MODE_CC),
            Map.entry("afterpay", List.of(".payment-image.adyenAfterPay")),
            Map.entry("afterpay installment", List.of(".payment-image.adyenAfterPayInstallment")),
            Map.entry("alipay", List.of(".payment-image.au-adyenAliPay")),
            Map.entry("khipu", List.of(".payment-image.khipu")),
            Map.entry("khipu bank transfer", List.of(".payment-image.khipu")),
            Map.entry("blik", List.of(".payment-image.p24-blik")),
            Map.entry("credit card", MODE_CC),
            Map.entry("cod", List.of(".payment-image.cod")),
            Map.entry("fbt", List.of(".payment-image.p24-fbt")),
            Map.entry("fast bank trasfer", List.of(".payment-image.p24-fbt")),
            Map.entry("heidi", List.of(".payment-image.HeidiPay")),
            Map.entry("heidi pay", List.of(".payment-image.HeidiPay")),
            Map.entry("heylight", List.of(".payment-image.HeidiPay")),
            Map.entry("klap", List.of(".payment-image.klap")),
            Map.entry("mach", List.of(".payment-image.mach")),
            Map.entry("master card", MODE_CC),
            Map.entry("mercado pago", List.of(".payment-image.mercadopago")),
            Map.entry("mercadopago", List.of(".payment-image.mercadopago")),
            Map.entry("one pay", List.of(".payment-image.flowOnePay")),
            Map.entry("onepay", List.of(".payment-image.flowOnePay")),
            Map.entry("paypal", List.of(".payment-image.adyenPaypal")),
            Map.entry("paypal express", List.of(".payment-image.adyenPaypalExpress")),
            Map.entry("servipag", List.of(".payment-image.servipag")),
            Map.entry("servipag bank transfer", List.of(".payment-image.servipag")),
            Map.entry("tbt", List.of(".payment-image.tbt")),
            Map.entry("visa card", MODE_CC),
            Map.entry("webpay", List.of(".payment-image.webpay")),
            Map.entry("wechat", List.of(".payment-image.au-adyenWeChat")));

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
            [data-an-tr='checkout-payment-detail'],
            .adyen-checkout__button--pay""";

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
            WebUI.wait(5).withMessage("pay now button enable").until(d -> !Form.isDisabled(elm));
            WebUI.scrollToCenter(elm);
            WebUI.delay(1);
            WebUI.click(elm);
            logger.info("Pay Now button clicked");
        } catch (Exception e) {
            throw new Exception("Unable to click Pay Now button", e);
        }
    }

    static void process(Context c) throws Exception {
        PaymentProcess p = c.paymentProcess;
        WebElement form = WebUI.waitElement(PAYMENT_FORM_LOCATOR, 15);
        WebUI.waitForNotPresent(".paymentmode-detail-loading", 15);
        switch (form.getTagName()) {
            case "app-payment-mode-afterpay": {
                payWithAfterPay(c);
                break;
            }
            case "app-payment-mode-adyen-alipay": {
                acceptTermAndConditions();
                clickPayNow();
                WebElement btn = WebUI.waitElement("#simulatePaymentFormId button[value='authorised']", 10);
                WebUI.click(btn);
                break;
            }
            case "app-payment-mode-p24-blik":
                payWithBlik(c, form);
                break;

            case
                    "app-card-on-delivery-payment",
                    "app-cod-payment":
                payWithCOD(c, form);
                break;

            case
                    "app-payment-mode-credit-card",
                    "app-payment-mode-kbank-credit-card",
                    "app-payment-mode-mercado-pago-credit":
                switch (p.methodName) {
                    case "amex card":
                        p.ccData = c.getProfile().getAmexCardData();
                        break;
                    case "master card":
                        p.ccData = c.getProfile().getMasterCardData();
                        break;
                    case "3ds card":
                        p.ccData = c.getProfile().getThreeDSCardData();
                        break;
                    default:
                        p.ccData = c.getProfile().getCreditCardData();
                        break;
                }
                payWithCreditCard(c, form);
                break;

            case "app-payment-mode-p24-fast-bank-transfer":
                payWithFBT(c, form);
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

            case "app-payment-mode-heidi-pay":
                payWithHeidi(c, form);
                break;

            case
                    "app-payment-mode-adyen-klarna",
                    "app-payment-mode-adyen-klarna-installment",
                    "app-payment-mode-klarna",
                    "app-payment-mode-klarna-de": {
                payWithKlarna(c, form);
                break;
            }
            case "app-payment-mode-wechat-pay": {
                acceptTermAndConditions();
                clickPayNow();
                break;
            }
            default:
                throw new Exception("Unknow payment mode " + form.getTagName());
        }
        logger.info("Payment process success");
    }

    static void payWithCOD(Context c, WebElement elm) throws Exception {
        try {
            acceptTermAndConditions();
            clickPayNow();
            WebUI.waitForUrlContains("/orderConfirmation", 15);
        } catch (Exception e) {
            throw new Exception("Unable to pay with COD", e);
        }
    }

    private static void handleCreditCardField(WebElement field, Context c) throws Exception {
        Map<String, String> data = c.paymentProcess.ccData;
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
                String cardNumber = data.get("cardNumber");
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
                String name = data.get("holderName");
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
                String expiryDate = data.get("expiryDate");
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
                String expiryMonth = data.get("expiryMonth");
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
                String expiryYear = data.get("expiryYear");
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
                String cvv = data.get("cvv");
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
        Map<String, String> data = c.getProfile().getPaypalData();
        try {
            acceptTermAndConditions();
            WebUI.delay(2);
            String iframeCSS = ".paypal-buttons iframe.component-frame";
            WebElement iframe = WebUI.findElement(iframeCSS);
            if (iframe == null) {
                WebUI.click(WebUI.findElement("""
                        .paypal-button-row div[role="link"][aria-label="PayPal"],
                        .paypal-button-row div[aria-label="PayPal Checkout"],
                        [data-an-la="payment:continue to paypal:paypal"]
                        """));
                WebUI.delay(2);
                WebUI.waitForUrlContains("https://www.sandbox.paypal.com/cgi-bin/webscr?", 30);
                WebUI.fill(".splitEmail input[name='login_email']", data.get("email"));
                WebUI.click(".splitEmail button.actionContinue");
                WebUI.delay(3);
                WebUI.wait(10);
                if (WebUI.waitElement("#otpVerification", 5) != null) {
                    WebUI.click(".tryAnotherWayLink  a");
                    WebUI.delay(1);
                    WebUI.click("li:not(.hide) a[aria-label='Login with password']");
                }
                WebUI.fill(".splitPassword input[name='login_password']", data.get("password"));
                WebUI.click(".splitPassword button.actionContinue ");
                WebUI.click(
                        ".CheckoutButton_buttonWrapper_2VloF button[data-testid='submit-button-initial'], button[data-testid='consentButton']");
                WebUI.waitForUrlContains("/orderConfirmation", 30);
            }
            WebUI.driver.switchTo().frame(iframe);
            logger.info("switched to iframe");
            WebUI.click(
                    ".paypal-button-row div[role='link'][aria-label='PayPal'], .paypal-button-row div[aria-label='PayPal Checkout'], [data-an-la='payment:continue to paypal:paypal']");
            WebUI.delay(2);
            WebUI.switchToWindow(1);
            try {
                WebUI.waitForUrlContains("https://www.sandbox.paypal.com/checkoutnow?", 30);
                if (WebUI.waitElement("[data-testid='basl-login-link-container'] button", 15) != null) {
                    WebUI.click("[data-testid='basl-login-link-container'] button");
                }
                WebUI.waitElement(".splitEmail input[name='login_email']", 5);
                WebUI.fill(".splitEmail input[name='login_email']", data.get("email"));
                if (WebUI.waitElement(".splitEmail button.actionContinue", 5) != null) {
                    WebUI.click(".splitEmail button.actionContinue");
                }
                if (WebUI.waitElement("#otpVerification", 5) != null) {
                    WebUI.click(".tryAnotherWayLink  a");
                    WebUI.delay(1);

                    WebUI.click("li:not(.hide) a[aria-label='Login with password']");
                }
                WebUI.fill("input[name='login_password']", data.get("password"));
                WebUI.click("button.actionContinue");
                WebUI.waitForUrlContains("https://www.sandbox.paypal.com/webapps/hermes?", 30);
                WebUI.click(
                        ".CheckoutButton_buttonWrapper_2VloF button[data-testid='submit-button-initial'], button[data-testid='consentButton']");
            } catch (Exception e) {
                WebUI.driver.close();
                WebUI.switchToWindow(0);
                throw e;
            }
            WebUI.switchToWindow(0);
            WebUI.waitForUrlContains("/orderConfirmation", 30);
        } catch (Exception e) {
            throw new Exception("Unable to pay with PayPal:", e);
        }
    }

    static void payWithAfterPay(Context c) throws Exception {
        Map<String, String> data = c.getProfile().getAfterPayData();
        acceptTermAndConditions();
        clickPayNow();
        WebUI.waitForUrlContains("afterpay.com", 15);
        WebUI.waitForPageLoad(15);
        WebElement elm = WebUI.waitElement("input[name='password']", 10);
        elm.clear();
        elm.sendKeys(data.get("password"));
        WebUI.click("button[type='submit']");
        WebUI.waitElement("button[data-dd-action-name='Confirm Checkout Button']", 30).click();
        WebUI.waitForUrlContains("/orderConfirmation", 10);
    }

    static void payWithBlik(Context c, WebElement elm) throws Exception {
        try {
            String blikCode = MockData.uniqueBlikCodes(1);
            logger.info("blikCode: " + blikCode);
            WebUI.fill("input[formcontrolname='blikT6Code']", blikCode);
            clickPayNow();
            WebUI.waitForUrlContains("/orderConfirmation", 15);
        } catch (Exception e) {
            throw new Exception("Unable to pay with Blik" + e.getMessage());
        }
    }

    static void payWithFBT(Context c, WebElement form) throws Exception {
        acceptTermAndConditions();
        clickPayNow();
        WebUI.wait(60, 2).withMessage("Pay with fast bank transfer").until(d -> {
            if (WebUI.getUrl().contains("/orderConfirmation"))
                return true;
            String to = """
                    [data-an-la="payment:pay now:internetbanka"],
                    [data-an-la='payment:pay now:internet banking'],
                    [data-an-la="payment:pay now:bank"],
                    .payment-group-label:has(img[alt='blik']),
                    [data-for='BLIK-null-null-tip'] div,
                    button[name='user_account_pbl[correct]']
                    """;
            List<WebElement> elms = WebUI.driver.findElements(By.cssSelector(to));
            for (WebElement elm : elms) {
                if (!elm.isDisplayed())
                    continue;
                WebUI.scrollToCenter(elm);
                WebUI.delay(1);
                elm.click();
            }
            return false;
        });
    }

    static void payWithGlow(Context c, WebElement elm) throws Exception {

    }

    static void payWithKlarna(Context c, WebElement elm) throws Exception {

    }

    static void payWithHeidi(Context c, WebElement elm) throws Exception {
        Map<String, String> data = c.getProfile().getHeidiData();
        try {
            acceptTermAndConditions();
            clickPayNow();
            WebUI.waitForUrlContains("https://sbx-checkout.heidipay.io/select-schedule", 30);
            WebUI.click(".MuiButtonBase-root:has(input[name='termsAndConditions'])");
            WebUI.click("#portal-continue-btn");
            WebUI.waitElement("#mobileNumber", 10);
            WebUI.fill("#mobileNumber", data.get("phoneNumber"));
            WebUI.click("#portal-continue-btn");
            for (WebElement e : WebUI.waitElements("[id*='mobileVerificationCode']", 10)) {
                int idx = Integer.parseInt(e.getDomAttribute("id").split("-")[1]);
                e.clear();
                e.sendKeys(String.valueOf(data.get("otp").charAt(idx)));
            }
            WebUI.click("#portal-continue-btn");
            WebUI.waitElement("#portal-infocert-widget", 10);
            WebUI.click("#portal-continue-btn");
            WebUI.waitElement("#portal-infocert-widget", 10);
            WebUI.delay(3);
            Map<String, String[]> fields = new LinkedHashMap<>();
            fields.put("cardNumber",
                    new String[] { "[title='Secure card number input frame']", "[name='cardnumber']" });
            fields.put("expiryDate",
                    new String[] { "[title='Secure expiration date input frame']", "[name='exp-date']" });
            fields.put("cvv", new String[] { "[title='Secure CVC input frame']", "[name='cvc']" });
            WebElement iframe = WebUI.findElement("[title='Enter card details']");
            WebUI.driver.switchTo().frame(iframe);
            logger.info("switched to outer iframe");
            // Fill in the fields in the child iframe
            for (Map.Entry<String, String[]> entry : fields.entrySet()) {
                String value = data.get(entry.getKey());
                String iframeSelector = entry.getValue()[0];
                String fieldSelector = entry.getValue()[1];

                WebUI.driver.switchTo().frame(WebUI.findElement(iframeSelector));
                WebUI.fill(fieldSelector, value);
                WebUI.driver.switchTo().parentFrame();
            }
            WebUI.fill("#cardholderName", data.get("holderName"));
            WebUI.delay(2);
            WebUI.click(".submit-button");
            WebUI.waitForUrlContains("/orderConfirmation", 60);
        } catch (Exception e) {
            throw new Exception("Unable to pay with HeyLight:", e);
        }

    }
}
