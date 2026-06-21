import practicum.api.HttpTaskServer;
import practicum.api.KVServer;
import practicum.enums.Status;
import practicum.managers.HistoryManager;
import practicum.managers.HttpTaskManager;
import practicum.managers.Managers;
import practicum.model.Epic;
import practicum.model.Subtask;
import practicum.model.Task;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    private static Scanner scanner;
    private static HttpTaskManager taskManager;
    private static HistoryManager historyManager;

    public static void main(String[] args) throws IOException {
        var kvserver = new KVServer();
        kvserver.start();

        Epic.DEFAULT_MANAGER = Managers.getDefault();

        var server = new HttpTaskServer();
        server.start();

        scanner = new Scanner(System.in);
        taskManager = (HttpTaskManager) Managers.getDefault();
        taskManager.historyManager = Managers.getDefaultHistory();
        historyManager = Managers.getDefaultHistory();
        printMenu();
        int input;

        // тестовые данные
        if (taskManager.getTasks().size() == 0) {
            taskManager.add(new Epic("Эпик 1", "Тестовое описание"), 0);
            taskManager.add(new Subtask("Подзадача 1", "Тестовое описание", 1), 0);
            taskManager.add(new Subtask("Подзадача 2", "Тестовое описание", 1), 0);
            taskManager.add(new Task("Задача 1", "Тестовое описание"), 0);
            taskManager.add(new Task("Задача 2", "Тестовое описание"), 0);
            taskManager.add(new Epic("Эпик 2", "Тестовое описание"), 0);
            taskManager.add(new Subtask("Подзадача 3", "Тестовое описание", 2), 0);
            taskManager.add(new Subtask("Подзадача 4", "Тестовое описание", 2), 0);
            taskManager.add(new Subtask("Подзадача 4", "Тестовое описание", 1), 0);

            var changedEpic = new Epic("Эпик 1 - новый", "Тестовое описание v2");
            changedEpic.setId(1);
            taskManager.change(changedEpic);
        }

        try {
            print("Созданные задачи");
            print(taskManager.getTasksInfo());
            print("Загруженные задачи");
            taskManager = (HttpTaskManager) HttpTaskManager.load("http://localhost:" + KVServer.PORT + "/");
            print(taskManager.getTasksInfo());
        } catch (Exception exception) {
            print("Загрузка работает некорректно, появляется исключение: " + exception.getMessage());
        }


        do {
            input = scanner.nextInt();
            switch (input) {
                case 1 -> print(taskManager.getTasksInfo()); // вывести все задачи
                case 2 -> createTask("Task"); // создать обычную задачу
                case 3 -> createTask("Epic"); // создать комплексную задачу
                case 4 -> createTask("Subtask"); // создать подзадачу
                case 5 -> changeStatus(); // сменить статус задачи
                case 6 -> deleteTask(); // удалить задачу
                case 7 -> printSubtasks(); // посмотреть подзадачи
                case 8 -> changeTask(); // изменить задачу
                case 9 -> taskManager.clear();
                case 10 -> showHistory();
                case 11 -> showPrioritizedTasks();
            }
        }
        while (input != 0);

        server.stop();
        kvserver.stop();
    }

    private static void showHistory() {
        print("Последние 10 просмотренных задач\n");
        var tasks = historyManager.getHistory();
        for (Task task : tasks) {
            print(task + "\n");
        }
    }

    private static void showPrioritizedTasks() {
        print("Задачи с сортировкой по времени\n");
        var tasks = taskManager.getPrioritizedTasks();
        for (Task task : tasks) {
            print(task + "");
        }
    }

    private static void changeTask() {
        int id = getInteger("Введите id задачи");
        var name = getTaskName();
        var description = getTaskDescription();

        var task = taskManager.find(id);
        task.setName(name);
        task.setDescription(description);

        var isCorrect = taskManager.change(task);
        if (isCorrect) {
            print("Задача успешно изменена");
        } else {
            print("Задача с таким id не существует");
        }
    }

    private static void printSubtasks() {
        int id = getInteger("Введите id комплексной задачи");
        var tasks = taskManager.getSubtasks(id);
        for (Task task : tasks) {
            print(task.toString());
        }
    }

    private static int getInteger(String message) {
        print(message);
        return scanner.nextInt();
    }

    private static void deleteTask() {
        int id = getInteger("Введите id удаляемой задачи");
        taskManager.remove(id);
    }

    private static void printMenu() {
        print("""
                1. Посмотреть все задачи
                2. Добавить обычную задачу
                3. Добавить комплексную задачу(Эпик)
                4. Добавить подзадачу
                5. Изменить статус
                6. Удалить задачу
                7. Посмотреть подзадачи
                8. Изменить название и описание
                9. Удалить все задачи
                10. Посмотреть историю
                11. Посмотреть все задачи, отсортированные по времени
                0. Выход""");
    }

    private static void print(String message) {
        System.out.println(message);
    }

    private static void changeStatus() {
        int id = getInteger("Введите id задачи, у которой меняем статус");
        int newStatus = getInteger(
                """
                        Введите новый статус
                        1. В процессе
                        2. Завершено""");

        var task = taskManager.find(id);
        if (task != null) {
            switch (newStatus) {
                case 1 -> task.setStatus(Status.IN_PROGRESS);
                case 2 -> task.setStatus(Status.DONE);
                default -> print("Такой статус не задан");
            }
            taskManager.change(task);
        } else {
            print("Такой задачи нет");
        }


    }

    private static void createTask(String type) {
        var name = getTaskName();
        var description = getTaskDescription();
        var isCorrect = false;

        switch (type) {
            case "Epic":
                isCorrect = taskManager.add(new Epic(name, description), 0);
                break;
            case "Subtask":
                int id = getInteger("Введите id комплексной задачи");
                var epic = taskManager.find(id);
                if (epic != null) {
                    isCorrect = taskManager.add(new Subtask(name, description, id), 0);
                } else {
                    print("Комплексной задачи с введенным id не существует");
                    return;
                }
                break;
            default:
                isCorrect = taskManager.add(new Task(name, description), 0);
                break;
        }
        if (isCorrect) {
            print("Задача успешно добавлена");
        } else {
            print("Не удалось добавить задачу");
        }
    }

    private static String getTaskDescription() {
        print("Введите описание");
        return scanner.nextLine();
    }

    private static String getTaskName() {
        print("Введите название");
        scanner.nextLine();
        return scanner.nextLine();
    }

}
