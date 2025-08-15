package wisewires.agent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public abstract class MockData {
    public static String IMEI() {
        int[] str = new int[15];
        int sum = 0, length = 15;

        // Danh sách RBI (Reporting Body Identifier)
        String[] rbiList = {
                "35", "44", "45", "49", "50", "51", "52",
                "53", "54", "86", "91", "98", "99"
        };

        // Chọn RBI ngẫu nhiên
        String rbi = rbiList[ThreadLocalRandom.current().nextInt(rbiList.length)];

        // Gán 2 chữ số đầu tiên
        str[0] = Character.getNumericValue(rbi.charAt(0));
        str[1] = Character.getNumericValue(rbi.charAt(1));

        // Sinh 12 chữ số tiếp theo
        for (int i = 2; i < length - 1; i++) {
            str[i] = ThreadLocalRandom.current().nextInt(10);
        }

        // Tính checksum theo thuật toán Luhn
        int len_offset = (length + 1) % 2;
        for (int i = 0; i < length - 1; i++) {
            int val = str[i];
            if ((i + len_offset) % 2 != 0) {
                val *= 2;
                if (val > 9)
                    val -= 9;
            }
            sum += val;
        }

        // Tính chữ số kiểm tra (checksum)
        str[length - 1] = (10 - (sum % 10)) % 10;

        // Kết hợp thành chuỗi IMEI
        StringBuilder imei = new StringBuilder();
        for (int d : str) {
            imei.append(d);
        }

        return imei.toString();
    }

    public static String SpanishIBAN() {
        Random rand = new Random();

        String bank = randomDigits(4, rand);
        String branch = randomDigits(4, rand);
        String account = randomDigits(10, rand);

        String cd1 = calculateCD(bank + branch, new int[] { 4, 8, 5, 10, 9, 7, 3, 6 });
        String cd2 = calculateCD(account, new int[] { 1, 2, 4, 8, 5, 10, 9, 7, 3, 6 });
        String cd = cd1 + cd2;

        String bban = bank + branch + cd + account;
        String numericCountry = "142800"; // E=14, S=28, plus '00'

        String ibanNumeric = bban + numericCountry;
        int checksum = 98 - mod97(ibanNumeric);
        return "ES" + String.format("%02d", checksum) + bban;
    }

    private static String randomDigits(int length, Random rand) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++)
            sb.append(rand.nextInt(10));
        return sb.toString();
    }

    private static String calculateCD(String number, int[] weights) {
        int sum = 0;
        for (int i = 0; i < weights.length; i++)
            sum += Character.getNumericValue(number.charAt(i)) * weights[i];
        int remainder = 11 - (sum % 11);
        if (remainder == 11)
            return "0";
        if (remainder == 10)
            return "1";
        return String.valueOf(remainder);
    }

    private static int mod97(String number) {
        String temp = "";
        for (int i = 0; i < number.length(); i += 9) {
            temp = Integer.toString(
                    Integer.parseInt(temp + number.substring(i, Math.min(i + 9, number.length()))) % 97);
        }
        return Integer.parseInt(temp);
    }

    public static String uniqueBlikCodes(int amount) throws Exception {
        if (amount > 999) {
            throw new Exception("The requested amount must be between 1 and 999");
        }
        Set<Integer> used = new HashSet<>();
        Random rand = new Random();
        List<String> result = new ArrayList<>();
        while (result.size() < amount) {
            int number = rand.nextInt(999) + 1;
            if (!used.contains(number)) {
                used.add(number);
                String code = String.format("777%03d", number);
                result.add(code);
            }
        }
        return result.get(0);
    }
}
