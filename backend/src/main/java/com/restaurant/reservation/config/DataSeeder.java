package com.restaurant.reservation.config;

import com.restaurant.reservation.model.Admin;
import com.restaurant.reservation.model.RestaurantTable;
import com.restaurant.reservation.repository.AdminRepository;
import com.restaurant.reservation.repository.TableRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final TableRepository tableRepository;

    public DataSeeder(AdminRepository adminRepository, TableRepository tableRepository) {
        this.adminRepository = adminRepository;
        this.tableRepository = tableRepository;
    }

    @Override
    public void run(String... args) {
        if (adminRepository.count() == 0) {
            adminRepository.save(new Admin("sri", "sri123"));
            System.out.println("Seeded default admin: username=sri, password=sri123");
        }
        if (tableRepository.count() == 0) {
            // table number, capacity, zone (tables of the same zone can be joined)
            Object[][] initial = {
                    {"1", 2, "WINDOW"},
                    {"2", 2, "WINDOW"},
                    {"3", 4, "WINDOW"},
                    {"4", 4, "WINDOW"},
                    {"5", 4, "MAIN"},
                    {"6", 6, "MAIN"},
                    {"7", 8, "PRIVATE"},
                    {"8", 10, "PRIVATE"}
            };
            for (Object[] t : initial) {
                tableRepository.save(new RestaurantTable((String) t[0], (Integer) t[1],
                        "AVAILABLE", (String) t[2]));
            }
            System.out.println("Seeded 8 default tables");
        } else {
            fillMissingZones();
        }
    }

    /** Tables created before zones existed get a zone so combos work out of the box. */
    private void fillMissingZones() {
        for (RestaurantTable table : tableRepository.findAll()) {
            if (table.getZone() == null || table.getZone().isBlank()) {
                table.setZone(defaultZone(table.getTableNumber(), table.getCapacity()));
                tableRepository.save(table);
            }
        }
    }

    private String defaultZone(String tableNumber, int capacity) {
        try {
            int number = Integer.parseInt(tableNumber.trim());
            if (number <= 4) {
                return "WINDOW";
            }
            return capacity >= 8 ? "PRIVATE" : "MAIN";
        } catch (NumberFormatException ex) {
            return "MAIN";
        }
    }
}