import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Recommender {
    /**
     * This map stores the movies rated for each user
     * Key - user_id
     * Value - A map with key=movie_id, value=rating
     */
    public static Map<Integer, Map<Integer, Integer>> userMap = new HashMap<>();

    public static Map<Integer, String> movieMap = new HashMap<>();

    /**
     * This function reads the csv file and initialize the data map
     *
     * @param csvFilePath - String
     */
    public static void initializeUsers(String csvFilePath) {
        String line;
        try {
            BufferedReader br = new BufferedReader(new FileReader(csvFilePath));
            // For all lines in the csv file
            while ((line = br.readLine()) != null) {
                String[] arr = line.split(",");
                int user_id = Integer.parseInt(arr[0]);
                int movie_id = Integer.parseInt(arr[1]);
                int rating = Integer.parseInt(arr[2]);

                // update map
                if (userMap.containsKey(user_id)) {
                    Map<Integer, Integer> map = userMap.get(user_id);
                    map.put(movie_id, rating);
                    userMap.put(user_id, map);
                } else {
                    Map<Integer, Integer> map = new HashMap<>();
                    map.put(movie_id, rating);
                    userMap.put(user_id, map);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void initializeMovies(String txtFilePath) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(txtFilePath));
            String line;
            while ((line = br.readLine()) != null) {
                // Split the line by the '|' character
                String[] parts = line.split("\\|");
                if (parts.length >= 2) {
                    int movieId = Integer.parseInt(parts[0]);
                    String movieTitle = parts[1];
                    movieMap.put(movieId, movieTitle);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This function computes the content rating distance given two users
     * and a minimum commonality threshold t.
     *
     * @param user1 - int user_id of first user
     * @param user2 - int user_id of second user
     * @param t     -- int minimum commonality threshold
     * @return A double representing the content rating distance between user1 and user2
     */
    public static double contentRatingDistance(int user1, int user2, int t) {
        // Form a map of movies user1 and user2 have in common
        Map<Integer, Integer> movies1 = userMap.get(user1);
        Map<Integer, Integer> movies2 = userMap.get(user2);
        List<Integer> commonMovies = new ArrayList<>();
        for (Integer m : movies2.keySet()) {
            if (movies1.containsKey(m)) commonMovies.add(m);
        }

        if (commonMovies.size() >= t) {
            // rating vector for 1
            List<Integer> ratings1 = new ArrayList<>();
            List<Integer> ratings2 = new ArrayList<>();
            for (Integer m : commonMovies) {
                ratings1.add(movies1.get(m));
                ratings2.add(movies2.get(m));
            }
            return angularDistance(ratings1, ratings2);
        }
        return 1.0;
    }

    /**
     * Utility function used by contentRatingDistance(u1, u2, t) to compute the
     * angularDistance (1 - cosine similarity) given two rating vectors.
     *
     * @param ratings1 - List of Integer
     * @param ratings2 - List of Integer
     * @return A double representing the angular distance of two rating vectors
     */
    public static double angularDistance(List<Integer> ratings1, List<Integer> ratings2) {
        if (ratings1.size() != ratings2.size()) {
            System.out.println("Their lengths are not equal.");
            return -1;
        }
        double dotProduct = 0;
        for (int i = 0; i < ratings1.size(); i++) {
            dotProduct += ratings1.get(i) * ratings2.get(i);
        }
        double magX = 0;
        for (Integer x : ratings1) {
            magX += x * x;
        }
        magX = Math.sqrt(magX);
        double magY = 0;
        for (Integer y : ratings2) {
            magY += y * y;
        }
        magY = Math.sqrt(magY);
        double cosineSimilarity = dotProduct / (magX * magY);
        return 1 - cosineSimilarity;
    }

    /**
     * This function returns the k other users from S with minimum content rating
     * distance to u with minimum commonality threshold t.
     *
     * @param u - int query user_id
     * @param S - A list of Integer user_id
     * @param t - int minimum commonality threshold
     * @param k - int
     */
    public static List<Integer> kNNSearch(int u, List<Integer> S, int t, int k) {
        // check
        if (k >= S.size()) return S;

        // calculate distances with each user in S
        List<double[]> neighbors = new ArrayList<>();
        for (Integer user : S) {
            if (user != u) {
                double d = contentRatingDistance(user, u, t);
                neighbors.add(new double[]{d, user});
            }
        }
        // sort distance list - O(SlogS)
        neighbors.sort(Comparator.comparingDouble(a -> a[0]));

        // return top k results
        int count = 0;
        List<Integer> res = new ArrayList<>();
        for (double[] d : neighbors) {
            if (count++ >= k) break;
            res.add((int) d[1]);
        }
        return res;
    }

    public static List<Integer> recommendations(int user, int r) {
        int k = 30;
        int t = 3;
        double p = 3.5;
        List<double[]> res = recHelper(user, r, k, t, p);
        List<Integer> movies = new ArrayList<>();
        // testing
        for (double[] d : res) {
            System.out.println(movieMap.get((int) d[1]) + " - Predicted Score: " + d[0]);
            movies.add((int) d[0]);
        }
        return movies;
    }

    public static List<double[]> recHelper(int user, int r, int k, int t, double p) {
        // Run kNNSearch on all users
        List<Integer> allUsers = new ArrayList<>();
        allUsers.addAll(userMap.keySet());
        List<Integer> N = kNNSearch(user, allUsers, t, k);

        // Find the all movies rated by at least one user in N but not user
        Map<Integer, long[]> M = new HashMap<>();
        // key: movie_id
        // value: (num_raters, sum_ratings)
        Map<Integer, Integer> userMovies = userMap.get(user);
        for (Integer u : N) {
            // get all movies u watched
            Map<Integer, Integer> neighborMovies = userMap.get(u);
            // for each movie that the neighbor has watched
            for (Integer m : neighborMovies.keySet()) {
                // if user did not watch it then
                if (!userMovies.containsKey(m)) {
                    // if m was added before
                    if (M.containsKey(m)) {
                        long[] curr = M.get(m);
                        curr[0] += 1;
                        // add neighbor rating to sum
                        curr[1] += neighborMovies.get(m);
                        M.put(m, curr);
                    } else {
                        long[] curr = new long[]{1, neighborMovies.get(m)};
                        M.put(m, curr);
                    }
                }
            }
        }
        List<double[]> recs = new ArrayList<>();
        // (smoothPrediction, movie_id)
        // Compute smoothed prediction for each movie in M
        for (Map.Entry<Integer, long[]> movie : M.entrySet()) {
            int m = movie.getKey();
            long[] value = movie.getValue();
            long Nj = value[0];
            long sum = value[1];
            double average = (double) sum / Nj;
            double s = smoothPrediction(p, Nj, average);
            // update res
            recs.add(new double[]{s, m});
        }
        recs.sort((a, b) -> Double.compare(b[0], a[0]));

        // return top r results
        List<double[]> res = new ArrayList<>();
        for (int i = 0; i < r; i++) {
            res.add(recs.get(i));
        }
        return res;
    }

    public static double smoothPrediction(double p, long numUsers, double averageRating) {
        double numerator = p + numUsers * averageRating;
        return numerator / (1 + numUsers);
    }

    public static void main(String[] args) {
        initializeUsers("ratings.csv");
        initializeMovies("movies.txt");
        recommendations(5002, 10);
    }
}
