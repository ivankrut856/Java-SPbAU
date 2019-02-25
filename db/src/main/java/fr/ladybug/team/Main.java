package fr.ladybug.team;

import fr.ladybug.team.mapping.Name;
import fr.ladybug.team.mapping.Phone;

import java.util.Iterator;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Phonebook phonebook = new Phonebook("phonebook_db");
        Scanner scanner = new Scanner(System.in);

        boolean isRunning = true;
        while(isRunning) {
            int command = askCommand(scanner);
            switch (command) {
                case 0: {
                    System.out.println("Bye");
                    isRunning = false;
                    break;
                }
                case 1: {
                    String name = askString(scanner, "Name: ");
                    String phone = askString(scanner, "Phone: ");
                    phonebook.addPair(name, phone);
                    System.out.println("Done");
                    break;
                }
                case 2: {
                    String name = askString(scanner, "Name: ");
                    Iterator<Phone> it = phonebook.getPhonesByName(name);
                    System.out.println("There is a full list of phone numbers of the person:");
                    it.forEachRemaining((x) -> System.out.println(x.getPhone()));
                    break;
                }
                case 3: {
                    String phone = askString(scanner, "Phone: ");
                    Iterator<Name> it = phonebook.getNamesByPhone(phone);
                    System.out.println("There is a full list of names associated with the phone:");
                    it.forEachRemaining((x) -> System.out.println(x.getName()));
                    break;
                }
                case 4: {
                    String name = askString(scanner, "Name: ");
                    String phone = askString(scanner, "Phone: ");
                    phonebook.removePair(name, phone);
                    System.out.println("Done");
                    break;
                }
                case 5: {
                    String name = askString(scanner, "Name: ");
                    String phone = askString(scanner, "Phone: ");
                    String newName = askString(scanner, "New name: ");
                    phonebook.removePair(name, phone);
                    phonebook.addPair(newName, phone);
                    System.out.println("Done");
                }
                case 6: {
                    String name = askString(scanner, "Name: ");
                    String phone = askString(scanner, "Phone: ");
                    String newPhone = askString(scanner, "New phone: ");
                    phonebook.removePair(name, phone);
                    phonebook.addPair(name, newPhone);
                    System.out.println("Done");
                }
                case 7: {
                    System.out.println("Complete dictionary:");
                    Iterator<Phonebook.NamePhonePair> it = phonebook.getAllPairs();
                    it.forEachRemaining(x -> {
                        System.out.print(x.name.getName());
                        System.out.print(" ");
                        System.out.println(x.phone.getPhone());
                    });
                    break;
                }

                default:
                    System.out.println("Write 0-7 to act");
            }
        }

    }

    private static int askCommand(Scanner scanner) {
        System.out.print("> ");
        var command = scanner.nextInt();
        scanner.nextLine();
        return command;
    }

    private static String askString(Scanner scanner, String promptMessage) {
        System.out.print(promptMessage);
        return scanner.nextLine();
    }
}
