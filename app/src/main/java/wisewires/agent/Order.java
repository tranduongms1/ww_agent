package wisewires.agent;

public abstract class Order {
    public static String getPONumber() {
        boolean isAE = WebUI.isOneOfSites("ae", "ae_ar");
        return WebUI.wait(10, 2).withMessage("get PO number").until(d -> {
            String text = WebUI.findElement(isAE ? ".order-number" : """
                    .checkout-success .order-number,
                    .order-number a.ng-star-inserted,
                    .order-number-summary .compact-details a,
                    .order-number-summary a:not([data-an-la]):not([target])""").getText().trim();
            return (isAE && text.length() > 18 || !isAE && text.length() < 18) ? text : null;
        });
    }
}
