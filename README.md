**Description:** This is a classic puzzle game where players are presented with a randomly generated board containing hidden mines. The goal is to reveal all non-mine tiles by clicking on them, while carefully avoiding any mines. Each revealed tile shows a number, indicating how many adjacent tiles contain mines. Using logic and strategy, players must flag tiles suspected of containing mines and clear the board without detonating any.

**How to run:**

1. **Install Java Development Kit (JDK)** You can download it from Oracle JDK or use OpenJDK. After installation, confirm it by running the following command in the terminal:  
   `java -version`

2. **Install Git**  
   Confirm the installation by running: `git --version`

3. **Clone Repository**  
   Type this into the terminal: `git clone https://github.com/junwaye/Minesweeper.git`

4. **Navigate to the project folder**  
   In the project directory after cloning, navigate by typing in the terminal: `cd Minesweeper/`

5. **Compile the Java Files**  
   Compile the `.java` files in the project by typing in the terminal: `javac -cp "libs/" -d bin src/**/*.java`

6. **Run the Main Class**  
   After compilation, run the main class by typing in the terminal: `java -cp "bin:libs/*" Main`

7. **Run Tests (Optional)**  
   If you'd like to run tests, execute: `java -cp "bin:libs/*" tester.Main`
