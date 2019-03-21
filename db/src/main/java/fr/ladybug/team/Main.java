package fr.ladybug.team;

import fr.ladybug.team.mapping.Name;
import fr.ladybug.team.mapping.Phone;

import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Phonebook phonebook = new Phonebook("phonebook_db");
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to the world greatest phonebook");
        printHelp();

        boolean isRunning = true;
        while (isRunning) {
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
                    try {
                        phonebook.addPair(name, phone);
                    }
                    catch (AlreadyExistsException e) {
                        System.out.println("Such name-phone pair already exists");
                        break;
                    }
                    System.out.println("Done");
                    break;
                }
                case 2: {
                    String name = askString(scanner, "Name:");
                    Iterator<Phone> it = phonebook.getPhonesByName(name);
                    System.out.println("There is a full list of phone numbers of the person:");
                    it.forEachRemaining((x) -> System.out.println(x.getPhone()));
                    break;
                }
                case 3: {
                    String phone = askString(scanner, "Phone:");
                    Iterator<Name> it = phonebook.getNamesByPhone(phone);
                    System.out.println("There is a full list of names associated with the phone:");
                    it.forEachRemaining((x) -> System.out.println(x.getName()));
                    break;
                }
                case 4: {
                    String name = askString(scanner, "Name:");
                    String phone = askString(scanner, "Phone:");

                    try {
                        phonebook.removePair(name, phone);
                    }
                    catch (NoSuchElementException e) {
                        System.out.println("No such name-phone pair in the phonebook");
                        break;
                    }
                    System.out.println("Done");
                    break;
                }
                case 5:
                case 6: {
                    String name = askString(scanner, "Name:");
                    String phone = askString(scanner, "Phone:");
                    String newName = name;
                    String newPhone = phone;
                    if (command == 5) {
                        newName = askString(scanner, "New name:");
                    }
                    else {
                        newPhone = askString(scanner, "New phone:");
                    }

                    try {
                        phonebook.changePair(name, phone, newName, newPhone);
                    } catch (AlreadyExistsException e) {
                        System.out.println("Such name-phone pair already exists, but done");
                    }
                    catch (NoSuchElementException e) {
                        System.out.println("No such name-phone pair");
                    }

                    System.out.println("Done");
                    break;
                }
                case 7: {
                    System.out.println("Complete dictionary:");
                    Iterator<Phonebook.NamePhonePair> it = phonebook.getAllPairs();
                    it.forEachRemaining(x -> {
                        System.out.print(x.name().getName());
                        System.out.print(" ");
                        System.out.println(x.phone().getPhone());
                    });
                    break;
                }

                default:
                    printHelp();
            }
            System.out.flush();
        }

    }

    private static int askCommand(Scanner scanner) {
        System.out.print("> ");
        System.out.flush();
        int command;
        try {
            command = scanner.nextInt();
        }
        catch (InputMismatchException e) {
            scanner.nextLine();
            System.out.println("Invalid input");
            System.out.flush();
            return -1;
        }
        scanner.nextLine();
        return command;
    }

    private static String askString(Scanner scanner, String promptMessage) {
        System.out.println(promptMessage);
        System.out.flush();
        return scanner.nextLine();
    }

    private static void printHelp() {
        System.out.println("Enter 0-7 to act:");
        System.out.println("0 to quit");
        System.out.println("1 to add new name-phone pair");
        System.out.println("2 to find all phones by name");
        System.out.println("3 to find all names by phone");
        System.out.println("4 to remove name-phone pair");
        System.out.println("5 to change name of name-phone pair");
        System.out.println("6 to change phone of name-phone pair");
        System.out.println("7 to print all name-phone pairs");
        System.out.flush();
    }
}
