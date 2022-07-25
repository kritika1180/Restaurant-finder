import java.util.*;
import java.io.*;

class coordinates {
    int lat;
    int lot;

    coordinates(int lat, int lot) {
        this.lat = lat;
        this.lot = lot;
    }

    void swapCrd(coordinates other){
        int temp1 = this.lat;
        int temp2 = this.lot;
        this.lat = other.lat;
        this.lot = other.lot;
        other.lat = temp1;
        other.lot = temp2;
    }
}

class trNode {
    trNode parent;
    trNode left;
    trNode right;
    int divVal;
    boolean isLeaf;
    int numberLeaves;
    int minX;
    int minY;
    int maxX;
    int maxY;
}

public class kdtree {

    public static trNode rootnode;

    static int partitionLat(ArrayList<coordinates> a, int start, int end) {
        int pivot = a.get(end).lat;
        int i = (start - 1);

        for (int j = start; j <= end - 1; j++) {
            if (a.get(j).lat < pivot) {
                i++;
                a.get(i).swapCrd(a.get(j));
            }
        }
        a.get(i+1).swapCrd(a.get(end));
        return (i + 1);
    }

    static int partitionLot(ArrayList<coordinates> a, int start, int end) {
        int pivot = a.get(end).lot;
        int i = (start - 1);

        for (int j = start; j <= end - 1; j++) {
            if (a.get(j).lot < pivot) {
                i++;
                a.get(i).swapCrd(a.get(j));
            }
        }
        a.get(i+1).swapCrd(a.get(end));
        return (i + 1);
    }

    static void quick(ArrayList<coordinates> a, int start, int end, boolean sortByLat) /* a[] = array to be sorted, start = Starting index, end = Ending index */ {
        if (start < end) {
            int p;
            if (sortByLat) {
                p = partitionLat(a, start, end);
                quick(a, start, p - 1, true);
                quick(a, p + 1, end, true);
            } else {
                p = partitionLot(a, start, end);
                quick(a, start, p - 1, false);
                quick(a, p + 1, end, false);
            }
        }
    }

    static void createTree(ArrayList<coordinates> a, trNode parentNode, int start, int end, boolean byLat, boolean isLeft) {
        quick(a, start, end, byLat);

        int medianIdx = (start + end) / 2;
        trNode newNode = new trNode();

        if (parentNode != null) {
            newNode.parent = parentNode;
            if (isLeft) {
                parentNode.left = newNode;
                if (byLat) {
                    newNode.minX = parentNode.minX;
                    newNode.maxX = parentNode.maxX;
                    newNode.minY = parentNode.minY;
                    newNode.maxY = parentNode.divVal;
                } else {
                    newNode.minX = parentNode.minX;
                    newNode.maxX = parentNode.divVal;
                    newNode.minY = parentNode.minY;
                    newNode.maxY = parentNode.maxY;
                }
            } else {
                parentNode.right = newNode;
                if (byLat) {
                    newNode.minX = parentNode.minX;
                    newNode.maxX = parentNode.maxX;
                    newNode.minY = parentNode.divVal;
                    newNode.maxY = parentNode.maxY;
                } else {
                    newNode.minX = parentNode.divVal;
                    newNode.maxX = parentNode.maxX;
                    newNode.minY = parentNode.minY;
                    newNode.maxY = parentNode.maxY;
                }
            }
        } else {
            newNode.maxX = Integer.MAX_VALUE;
            newNode.maxY = Integer.MAX_VALUE;
            newNode.minX = Integer.MIN_VALUE;
            newNode.minY = Integer.MIN_VALUE;
            rootnode = newNode;
        }

        newNode.numberLeaves = end - start + 1;
        if (byLat) newNode.divVal = a.get(medianIdx).lat;
        else newNode.divVal = a.get(medianIdx).lot;

        if (start == end) {
            newNode.isLeaf = true;
            newNode.minX = newNode.maxX = a.get(medianIdx).lat;
            newNode.minY = newNode.maxY = a.get(medianIdx).lot;
        } else {
            newNode.isLeaf = false;
            createTree(a, newNode, start, medianIdx, !byLat, true);
            createTree(a, newNode, medianIdx + 1, end, !byLat, false);
        }
    }

    static boolean overlaps(trNode curNode, int xCrd, int yCrd){
        if(curNode.minX < xCrd - 100 && curNode.maxX < xCrd - 100)
            return false;
        if(curNode.minY < yCrd - 100 && curNode.maxY < yCrd - 100)
            return false;
        if(curNode.minX > xCrd + 100 && curNode.maxX > xCrd + 100)
            return false;
        if(curNode.minY > yCrd + 100 && curNode.maxY > yCrd + 100)
            return false;

        return true;
    }

    static int findRstr(trNode curNode, int xQry, int yQry) {
        if (curNode.isLeaf) {
            if ((curNode.minX >= xQry - 100 && curNode.minY >= yQry - 100) && (curNode.maxX <= xQry + 100 && curNode.maxY <= yQry + 100)) {
//                System.out.println("this returned 1");
                return 1;
            } else
                return 0;
        }
        int ans = 0;
        if ((curNode.left.minX > xQry - 100 && curNode.left.minY > yQry - 100)
                && (curNode.left.maxX <= xQry + 100 && curNode.left.maxY <= yQry + 100)) {
//            System.out.println("Added left");
            ans += curNode.left.numberLeaves;
        }
        else if (overlaps(curNode.left, xQry, yQry)) {
            ans += findRstr(curNode.left, xQry, yQry);
        }

        if ((curNode.right.minX > xQry - 100 && curNode.right.minY > yQry - 100)
                && (curNode.right.maxX <= xQry + 100 && curNode.right.maxY <= yQry + 100)) {
//            System.out.println("Added right");
            ans += curNode.right.numberLeaves;
        }
        else if (overlaps(curNode.right, xQry, yQry)) {
            ans += findRstr(curNode.right, xQry, yQry);
        }
        return ans;
    }

    public static void main(String[] args) {

        ArrayList<coordinates> rstr = new ArrayList<coordinates>();
        ArrayList<coordinates> quer = new ArrayList<coordinates>();

        try (BufferedReader in = new BufferedReader(new FileReader("restaurants.txt"))) {
            String str;
            in.readLine();
            while ((str = in.readLine()) != null) {
                String[] crd = str.split(",");
                coordinates newCrd = new coordinates(Integer.parseInt(crd[0]), Integer.parseInt(crd[1]));
                rstr.add(newCrd);
            }
        } catch (IOException e) {
            System.out.println("Restaurants File Read Error");
        }
        try (BufferedReader in = new BufferedReader(new FileReader("queries.txt"))) {
            String str;
            in.readLine();
            while ((str = in.readLine()) != null) {
                String[] crd = str.split(",");
                coordinates newCrd = new coordinates(Integer.parseInt(crd[0]), Integer.parseInt(crd[1]));
                quer.add(newCrd);
            }
        } catch (IOException e) {
            System.out.println("Queries File Read Error");
        }

        createTree(rstr, null, 0, rstr.size() - 1, true, true);
        int ans = findRstr(rootnode, 0, 0);

        try {
            FileOutputStream fs = new FileOutputStream ("output.txt",true );
            PrintStream p = new PrintStream ( fs );
            for (int i = 0; i < quer.size(); i++) {
//                System.out.println(quer.get(i).lat + " : " + quer.get(i).lot);
                p.println(findRstr(rootnode, quer.get(i).lat, quer.get(i).lot));
            }
        } catch (IOException e) {
            System.out.println("Queries File Read Error");
        }
    }
}