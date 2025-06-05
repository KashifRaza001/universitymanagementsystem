# University Management System

A comprehensive University Management System developed using Java (JDK 21) and NetBeans. This application streamlines administrative tasks, including student enrollment, course management, and examination processes, providing an efficient solution for university operations.

## Features

* **Student Enrollment**: Register and manage student information seamlessly.
* **Course Management**: Create, update, and assign courses to students and faculty.
* **Faculty Management**: Maintain faculty profiles and their associated courses.
* **Examination Module**: Schedule exams, input grades, and generate reports.
* **User Authentication**: Secure login system for administrators, faculty, and students.
* **Reporting**: Generate detailed reports on student performance and course statistics.

## Technologies Used

* **Programming Language**: Java (JDK 21)
* **IDE**: NetBeans
* **Database**: MySQL
* **Build Tool**: Apache Ant

## Installation

1. **Clone the Repository**:

   ```bash
   git clone https://github.com/KashifRaza001/universitymanagementsystem.git
   ```

2. **Import Project**:

   * Open NetBeans.
   * Navigate to `File` > `Open Project`.
   * Select the cloned repository folder.

3. **Set Up Database**:

   * Ensure MySQL is installed and running.
   * Create a new database (e.g., `university_db`).
   * Import the `new.sql` file located in the project root to set up the necessary tables.

4. **Configure Database Connection**:

   * Open the project in NetBeans.
   * Navigate to the database configuration file (e.g., `dbconfig.properties`).
   * Update the database URL, username, and password as per your MySQL setup.

5. **Build and Run**:

   * Right-click on the project in NetBeans.
   * Select `Clean and Build`.
   * After a successful build, right-click and select `Run`.

## Usage

* **Admin Module**:

  * Login with admin credentials.
  * Manage students, faculty, courses, and examinations.

* **Faculty Module**:

  * Login with faculty credentials.
  * View assigned courses and input student grades.

* **Student Module**:

  * Login with student credentials.
  * View enrolled courses, examination schedules, and grades.

## Contributing

Contributions are welcome! Please fork the repository and submit a pull request for any enhancements or bug fixes.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Acknowledgments

Special thanks to [Kashif Raza](https://github.com/KashifRaza001) for developing this comprehensive University Management System.
