package com.laioffer.classone;

import java.io.*;
import java.util.*;

public class Homework3 {

    /**
     * The class "Predicate" is the main object worked on in this program. Basically a Predicate class means such a String:
     *
     * "~A(x,y,z)"
     *
     * We can see it as an entry in a clause.
     * It has two fields, String name and List<String> var. String name means the prefix of the predicate, which is "~A" here.
     * The List<String> var is the list of variables in this Predicate, which is [x,y,z] here.
     *
     * So a clause consists of several Predicate objects. I use a map to store them. The key is the name of that Predicate,
     * and the value is the Predicate itself. So a clause is a Map.
     *
     * At last, the Knowledge Base is a Map. The keys are the names of the Predicates that has appeared, and the values are
     * the clauses that this name appears in, which is represented by a List of Map (list of clauses).
     */

    private static Map<String, List<Map<String, Predicate>>> KB;


    static class Predicate {
        String name;
        List<String> var;
        public Predicate(String s) {
            this.name = s;
            this.var = new ArrayList<>();
        }
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Predicate)) {
                return false;
            }
            Predicate another = (Predicate) obj;
            return this.name.equals(another.name) && this.var.equals(another.var);
        }

        @Override
        public int hashCode() {
            return name.hashCode() * 31 + var.hashCode();
        }
    }

    public static void main (String[] args) {
        try {
                File fileName = new File("input.txt");
                BufferedReader br = new BufferedReader(new FileReader(fileName));
                String line = "";
                line = br.readLine();
                int numQuery = Integer.parseInt(line.trim());
                List<String> queries = new ArrayList<>();
                for (int i = 0; i <= numQuery - 1; i++) {
                    line = br.readLine();
                    queries.add(line.trim());
                }
                line = br.readLine();
                int numRule = Integer.parseInt(line.trim());
                List<String> rules = new ArrayList<>();
                for (int i = 0; i <= numRule - 1; i++) {
                    line = br.readLine();
                    rules.add(line.trim());
                }
                br.close();
                File writeName = new File("output.txt");
                if (!writeName.exists()) {
                    writeName.createNewFile();
                }
                BufferedWriter bw = new BufferedWriter(new FileWriter(writeName));
                for (String query : queries) {
                    KB = new HashMap<>();
                    Map<String, Integer> used = new HashMap<>(); // record the name of variables that has been used
                    for (String rule : rules) {
                        insertKB(rule, used);
                    }
                    String temp = negation(query);
                    insertKB(temp, used);
                    bw.write(judge());
                    bw.write("\n");
                }
                bw.flush();
                bw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Insert a clause into the knowledge base
     * @param clause
     * @param used Used to record the number of same parameters in different clauses, and rename the duplicate parameters
     */
    private static void insertKB(String clause, Map<String, Integer> used) {
        List<String> names = new ArrayList<>(); // all of the predicates' names in this clause
        Map<String, Predicate> map = new HashMap<>();  // all of the wrapped predicates in this clause
        String[] predicates = clause.split("\\|");
        Set<String> appeared = new HashSet<>(); // record all the variables and constants in this clause

        for (String s : predicates) {
            s = s.trim();
            StringBuilder sb = new StringBuilder();
            int index;
            if (s.charAt(0) == '~') {
                sb.append("~");
                index = 1;
            } else {
                index = 0;
            }
            while (index <= s.length() - 1 && s.charAt(index) != '(') {
                sb.append(s.charAt(index++));
            }
            String name = sb.toString();
            names.add(name);
            Predicate predicate = new Predicate(name);
            List<String> variable = new ArrayList<>();
            int start = index + 1;
            while (index < s.length() - 1) {
                index++;
            }
            String temp = s.substring(start, index);
            String[] vars = temp.split(",");
            for (String var : vars) {
                var = var.trim();
                appeared.add(var);
                if (var.charAt(0) <= 'Z') {
                    variable.add(var);
                } else if (used.get(var) == null) {
                    variable.add(var + "1");
                    used.put(var, 1);
                } else {
                    int count = used.get(var);
                    variable.add(var + String.valueOf(count));
                }
            }
            predicate.var = variable;
            map.put(name, predicate);
        }

        for (String var : appeared) {
            if (var.charAt(0) >= 'a') {
                used.put(var, used.get(var) + 1);
            }
        }

        for (String name : names) {
            if (KB.get(name) == null) {
                KB.put(name, new ArrayList<>());
            }
            KB.get(name).add(map);
        }
    }

    /**
     * Get the negation of the given string
     * @param s
     * @return
     */
    private static String negation(String s) {
        if (s.charAt(0) != '~') {
            return "~" + s;
        } else {
            return s.substring(1);
        }
    }

    /**
     * The main function in this homework, juding whether the given query is true or not
     * @return a String that the output wants.
     */

    private static String judge() {

        Map<Map<String, Predicate>, Set<Map<String, Predicate>>> compared = new HashMap<>(); // Don't resolve the same pair for two times.

        while (true) {
            Set<String> usedPair = new HashSet<>(); // For each pair of X and ~X, do one time's resolve
            boolean[] loop = new boolean[] {true}; // control infinite loop
            for (Map.Entry<String, List<Map<String, Predicate>>> entry : KB.entrySet()) {
                if (usedPair.add(entry.getKey())) {
                    String s1 = entry.getKey();
                    String s2 = negation(s1);
                    usedPair.add(s2);
                    List<Map<String, Predicate>> list1 = KB.get(s1);
                    List<Map<String, Predicate>> list2 = KB.get(s2);
                    if (list2 == null) {
                        continue;
                    }
                    List<Map<String, Map<String, Predicate>>> avoidConcurrencyExp = new ArrayList<>(); // store some entries temporarily to avoid concurrency exception

                    for (Map<String, Predicate> map1 : list1) {
                        for (Map<String, Predicate> map2 : list2) {
                            if (compared.get(map1) != null && compared.get(map1).contains(map2) || compared.get(map2) != null && compared.get(map2).contains(map1)) {
                                continue;
                            } else {
                                if (compared.get(map1) == null) {
                                    compared.put(map1, new HashSet<>());
                                }
                                if (compared.get(map2) == null) {
                                    compared.put(map2, new HashSet<>());
                                }
                                if (map1.equals(map2)) {
                                    continue;
                                } else if (map1.size() == 1) {
                                    compared.get(map1).add(map2);
                                    compared.get(map2).add(map1);
                                    if (!resolve(map1, map2, s1, s2, loop, avoidConcurrencyExp)) {
                                        return "TRUE";
                                    }
                                } else if (map2.size() == 1) {
                                    compared.get(map1).add(map2);
                                    compared.get(map2).add(map1);
                                    if (!resolve(map2, map1, s2, s1, loop, avoidConcurrencyExp)) {
                                        return "TRUE";
                                    }
                                }
                            }
                        }
                    }
                    for (Map<String, Map<String, Predicate>> concurrEntryMap : avoidConcurrencyExp) {
                        for (Map.Entry<String, Map<String, Predicate>> concurrEntry : concurrEntryMap.entrySet()) {
                            KB.get(concurrEntry.getKey()).add(concurrEntry.getValue());
                        }
                    }
                }
            }
//            System.out.println(loop[0]);
            if (loop[0]) { // if in this for-for loop, we don't resolve any one pair
                return "FALSE";
            }
        }
    }

    /**
     * Resolve the two grabbed clauses
     * @param shorter the shorter clause (length = 1), which is a fact
     * @param longer the longer clause with uncertain length
     * @param name1 the predicate of the shorter clause
     * @param name2 the corresponding (negation) predicate in the longer clause
     * @param loop if one resolve can be done successfully, then this boolean variable will be false
     * @param avoidConcurrencyExp avoid concurrency exception when adding elements to a list while iterating it
     * @return false if a contradiction is found, true if we should continue
     */

    private static boolean resolve(Map<String, Predicate> shorter, Map<String, Predicate> longer, String name1, String name2, boolean[] loop, List<Map<String, Map<String, Predicate>>> avoidConcurrencyExp) {
        Predicate p1 = shorter.get(name1);
        Predicate p2 = longer.get(name2);

        Map<String, Predicate> res = new HashMap<>();

        if (!unify(longer, p1, p2, res)) {
            return true; // Cannot unify, but doesn't mean resolve to null.
        }

        System.out.println("Resolving:" + name1 + "(" + p1.var + ")");
        System.out.println("and" + name2 + ":");
        for (Map.Entry<String, Predicate> entry : longer.entrySet()) {
            System.out.println(entry.getValue().var);
        }
        System.out.println(" The result has size = " + res.size());

        if (res.size() == 0) {
            return false;
        }

        for (Map.Entry<String, Predicate> entry : res.entrySet()) {
            if (!entry.getKey().equals(name1)) {
                KB.get(entry.getKey()).add(res);
            } else {
                Map<String, Map<String, Predicate>> temp = new HashMap<>();
                temp.put(entry.getKey(), res);
                avoidConcurrencyExp.add(temp);
            }
        }
        loop[0] = false;
        return true;
    }

    /**
     * Make the variables or constants in the two clauses be consistent
     * @param longer the longer clause
     * @param p1 one of the predicate
     * @param p2 the negation of p1
     */

    private static boolean unify(Map<String, Predicate> longer, Predicate p1, Predicate p2, Map<String, Predicate> res) {
        Map<String, String> varMap = new HashMap<>();

        List<String> var1 = p1.var;
        List<String> var2 = p2.var;

        // First, we want to know if there are any lower case in the var1.
        // If all of the variables in var1 are lower case, then p1 is a universally correct fact without any specific constants.
        // We just need to remove the corresponding predicate in longer and keep all the rest unchanged.

        // If some of the variables in var1 are lower case and some are upper case, for those upper-cased, we need to do variable transformation
        // If all of the variables in var1 are upper case, perform all the variable transformations.

        for (int i = 0; i <= var1.size() - 1; i++) {
            String s1 = var1.get(i);
            String s2 = var2.get(i);
            // s1 is fact (the shorter one)
            // 1. if s1 is lowercase, we don't care about the corresponding letter in s2. (Just skip it)
            // 2. if s1 is uppercase and s2 is lowercase, transform the s2.
            // 2. if s1 and s2 are both uppercase, if they are not the same, we cannot resolve
            if (s1.charAt(0) >= 'a') {
                continue;
            } else if (s1.charAt(0) <= 'Z' && s2.charAt(0) >= 'a') {
                varMap.put(s2, s1);
            } else if (!s1.equals(s2)){
                return false;
            }
        }

        for (Map.Entry<String, Predicate> entry : longer.entrySet()) {
            if (!entry.getKey().equals((p2.name))) {
                // make a copy of current Predicate
                Predicate p = new Predicate(entry.getKey());
                for (String var : entry.getValue().var) {
                    if (varMap.get(var) == null) {
                        p.var.add(var);
                    } else {
                        p.var.add(varMap.get(var));
                    }
                }
                res.put(entry.getKey(), p);
            }
        }
        return true;
    }


}
