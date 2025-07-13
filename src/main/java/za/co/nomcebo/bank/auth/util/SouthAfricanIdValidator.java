package za.co.nomcebo.bank.auth.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * South African ID number validator and utility class.
 * <p>
 * Validates South African ID numbers according to the official format:
 * YYMMDD-GGGG-SCZ
 * </p>
 * <p>
 * Where:
 * - YY: Year of birth (last two digits)
 * - MM: Month of birth (01-12)
 * - DD: Day of birth (01-31)
 * - GGGG: Gender and sequence number (0000-4999 = female, 5000-9999 = male)
 * - S: South African citizenship (0 = SA citizen, 1 = permanent resident)
 * - C: Usually 8 or 9 (race classification - now obsolete but still used)
 * - Z: Checksum digit calculated using Luhn algorithm
 * </p>
 * This validator ensures compliance with South African identity verification
 * requirements for banking KYC (Know Your Customer) processes.
 *
 * @author Nomcebo Bank Development Team
 * @version 1.0.0
 * @since 2025-07-10
 */
@Component
@Slf4j
public class SouthAfricanIdValidator {

    private static final Pattern SA_ID_PATTERN = Pattern.compile("^\\d{13}$");
    private static final int SA_ID_LENGTH = 13;

    /**
     * Validates a South African ID number.
     * <p>
     * Performs comprehensive validation including:
     * - Format validation (13 digits)
     * - Date validation (valid birthdate)
     * - Checksum validation using Luhn algorithm
     *</p>
     * @param saIdNumber The SA ID number to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidSaIdNumber(String saIdNumber) {
        if (saIdNumber == null || saIdNumber.trim().isEmpty()) {
            log.debug("SA ID validation failed: null or empty input");
            return false;
        }

        // Remove any spaces or dashes
        String cleanedId = saIdNumber.replaceAll("[\\s-]", "");

        // Check format (13 digits)
        if (!SA_ID_PATTERN.matcher(cleanedId).matches()) {
            log.debug("SA ID validation failed: invalid format for ID {}", saIdNumber);
            return false;
        }

        // Validate date of birth
        if (!isValidBirthDate(cleanedId)) {
            log.debug("SA ID validation failed: invalid birth date for ID {}", saIdNumber);
            return false;
        }

        // Validate checksum
        if (!isValidChecksum(cleanedId)) {
            log.debug("SA ID validation failed: invalid checksum for ID {}", saIdNumber);
            return false;
        }

        log.debug("SA ID validation successful for ID {}", saIdNumber);
        return true;
    }

    /**
     * Extracts date of birth from SA ID number.
     *
     * @param saIdNumber Valid SA ID number
     * @return LocalDateTime representing birthdate
     * @throws IllegalArgumentException if ID is invalid
     */
    public LocalDateTime extractDateOfBirth(String saIdNumber) {
        if (!isValidSaIdNumber(saIdNumber)) {
            throw new IllegalArgumentException("Invalid SA ID number");
        }

        String cleanedId = saIdNumber.replaceAll("[\\s-]", "");

        int year = Integer.parseInt(cleanedId.substring(0, 2));
        int month = Integer.parseInt(cleanedId.substring(2, 4));
        int day = Integer.parseInt(cleanedId.substring(4, 6));

        // Determine century (assume cutoff at 25 for current century)
        int currentYear = LocalDateTime.now().getYear();
        int centuryThreshold = currentYear % 100 - 25;

        if (year <= centuryThreshold) {
            year += 2000;
        } else {
            year += 1900;
        }

        return LocalDateTime.of(year, month, day, 0, 0);
    }

    /**
     * Extracts gender from SA ID number.
     *
     * @param saIdNumber Valid SA ID number
     * @return "M" for male, "F" for female
     * @throws IllegalArgumentException if ID is invalid
     */
    public String extractGender(String saIdNumber) {
        if (!isValidSaIdNumber(saIdNumber)) {
            throw new IllegalArgumentException("Invalid SA ID number");
        }

        String cleanedId = saIdNumber.replaceAll("[\\s-]", "");
        int genderDigit = Integer.parseInt(cleanedId.substring(6, 10));

        return genderDigit >= 5000 ? "M" : "F";
    }

    /**
     * Checks if the person is a South African citizen.
     *
     * @param saIdNumber Valid SA ID number
     * @return true if SA citizen, false if permanent resident
     * @throws IllegalArgumentException if ID is invalid
     */
    public boolean isSouthAfricanCitizen(String saIdNumber) {
        if (!isValidSaIdNumber(saIdNumber)) {
            throw new IllegalArgumentException("Invalid SA ID number");
        }

        String cleanedId = saIdNumber.replaceAll("[\\s-]", "");
        int citizenshipDigit = Integer.parseInt(cleanedId.substring(10, 11));

        return citizenshipDigit == 0;
    }

    /**
     * Calculates age from SA ID number.
     *
     * @param saIdNumber Valid SA ID number
     * @return Age in years
     * @throws IllegalArgumentException if ID is invalid
     */
    public int calculateAge(String saIdNumber) {
        LocalDateTime birthDate = extractDateOfBirth(saIdNumber);
        LocalDateTime now = LocalDateTime.now();

        int age = now.getYear() - birthDate.getYear();

        // Adjust if birthday hasn't occurred this year
        if (now.getMonthValue() < birthDate.getMonthValue() ||
                (now.getMonthValue() == birthDate.getMonthValue() &&
                        now.getDayOfMonth() < birthDate.getDayOfMonth())) {
            age--;
        }

        return age;
    }

    /**
     * Validates the birthdate portion of the SA ID.
     *
     * @param saIdNumber 13-digit SA ID number
     * @return true if birthdate is valid
     */
    private boolean isValidBirthDate(String saIdNumber) {
        try {
            int year = Integer.parseInt(saIdNumber.substring(0, 2));
            int month = Integer.parseInt(saIdNumber.substring(2, 4));
            int day = Integer.parseInt(saIdNumber.substring(4, 6));

            // Basic range checks
            if (month < 1 || month > 12) {
                return false;
            }

            if (day < 1 || day > 31) {
                return false;
            }

            // Determine full year
            int currentYear = LocalDateTime.now().getYear();
            int centuryThreshold = currentYear % 100 - 25;
            int fullYear = (year <= centuryThreshold) ? year + 2000 : year + 1900;

            // Check if date is valid using LocalDateTime
            LocalDateTime.of(fullYear, month, day, 0, 0);

            // Check if birthdate is not in the future
            LocalDateTime birthDate = LocalDateTime.of(fullYear, month, day, 0, 0);
            return !birthDate.isAfter(LocalDateTime.now());

        } catch (Exception e) {
            log.debug("Birth date validation failed for SA ID: {}", saIdNumber, e);
            return false;
        }
    }

    /**
     * Validates the checksum digit using Luhn algorithm.
     *
     * @param saIdNumber 13-digit SA ID number
     * @return true if checksum is valid
     */
    private boolean isValidChecksum(String saIdNumber) {
        try {
            // Extract first 12 digits for checksum calculation
            String firstTwelveDigits = saIdNumber.substring(0, 12);
            int providedChecksum = Integer.parseInt(saIdNumber.substring(12, 13));

            // Calculate checksum using Luhn algorithm
            int sum = 0;
            boolean alternate = false;

            // Process digits from right to left
            for (int i = firstTwelveDigits.length() - 1; i >= 0; i--) {
                int digit = Integer.parseInt(String.valueOf(firstTwelveDigits.charAt(i)));

                if (alternate) {
                    digit *= 2;
                    if (digit > 9) {
                        digit = (digit % 10) + 1;
                    }
                }

                sum += digit;
                alternate = !alternate;
            }

            // Calculate expected checksum
            int expectedChecksum = (10 - (sum % 10)) % 10;

            return expectedChecksum == providedChecksum;

        } catch (Exception e) {
            log.debug("Checksum validation failed for SA ID: {}", saIdNumber, e);
            return false;
        }
    }

    /**
     * Formats SA ID number with standard dashes.
     *
     * @param saIdNumber 13-digit SA ID number
     * @return Formatted ID (YYMMDD-GGGG-SCZ)
     * @throws IllegalArgumentException if ID is invalid
     */
    public String formatSaIdNumber(String saIdNumber) {
        if (!isValidSaIdNumber(saIdNumber)) {
            throw new IllegalArgumentException("Invalid SA ID number");
        }

        String cleanedId = saIdNumber.replaceAll("[\\s-]", "");
        return cleanedId.substring(0, 6) + "-" +
                cleanedId.substring(6, 10) + "-" +
                cleanedId.substring(10, 13);
    }
}