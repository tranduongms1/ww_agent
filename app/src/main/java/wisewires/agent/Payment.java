package wisewires.agent;

import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Payment {
    static Logger logger = LoggerFactory.getLogger(Payment.class);

    static String PAYMENT_MODE_LOCATOR = """
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

    static void process(Context c) throws Exception {
        WebElement form = WebUI.waitElement(PAYMENT_MODE_LOCATOR, 15);
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
    }

    static void payWithCOD(Context c, WebElement elm) throws Exception {

    }

    static void payWithCreditCard(Context c, WebElement elm) throws Exception {

    }

    static void payWithPayPal(Context c, WebElement elm) throws Exception {

    }

    static void payWithGlow(Context c, WebElement elm) throws Exception {

    }

    static void payWithKlarna(Context c, WebElement elm) throws Exception {

    }
}
