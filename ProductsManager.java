import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.derby.jdbc.EmbeddedDriver;

import java.sql.*;
import java.util.*;

public class ProductsManager extends Application {

    private ProductsDataController dbManager;
    private ObservableList<Product> userProducts, executiveProducts;
    private TableView<Product> userView, executiveView;

    private final String sold="sold", added = "added", sorted = "sorted";

    private EventManager eventManager;

    @Override
    public void start(Stage myStage) {

        dbManager = new ProductsDataController();

        myStage.setTitle("Products Manager");

        GridPane rootNode = new GridPane();
        rootNode.setPadding(new Insets(15));
        rootNode.setHgap(5);
        rootNode.setVgap(5);
        rootNode.setAlignment(Pos.CENTER);

        Scene myScene = new Scene(rootNode, 900, 600);

        // ======================
        // add panel
        // ======================

        GridPane addPane = new GridPane();
        addPane.setPadding(new Insets(15));
        addPane.setHgap(5);
        addPane.setVgap(5);
        addPane.setAlignment(Pos.CENTER);

        // product name
        addPane.add(new Label("Product Name:"), 0, 0);
        TextField txtProductName = new TextField();
        addPane.add(txtProductName, 1, 0);

        // product count
        addPane.add(new Label("Product Count:"), 0, 1);
        TextField txtProductCount = new TextField();
        addPane.add(txtProductCount, 1, 1);

        // add product
        Button btnAdd = new Button("Add Product");
        addPane.add(btnAdd, 1, 2);
        GridPane.setHalignment(btnAdd, HPos.LEFT);        
        
        // status label
        Label addStatus = new Label();
        GridPane.setColumnSpan(addStatus, 2);
        addPane.add(addStatus, 0, 3);
        GridPane.setHalignment(addStatus, HPos.CENTER);

        // add handler
        btnAdd.setOnAction(e -> {

            String productName = txtProductName.getText();
            int count = Integer.parseInt(txtProductCount.getText());

            Product product = new Product();
            product.setName(productName);
            product.setOriginalCount(count);

            EventObject event = new EventObject(product);
            event.setStatusLabel(addStatus);
            eventManager.notify(added, event);

        });

        GridPane.setValignment(addPane, VPos.BOTTOM);

        rootNode.add(addPane, 0, 0);

        // ======================
        // add panel
        // ======================

        GridPane salePanel = new GridPane();
        salePanel.setPadding(new Insets(15));
        salePanel.setHgap(5);
        salePanel.setVgap(5);
        salePanel.setAlignment(Pos.CENTER);

        // product name
        salePanel.add(new Label("Product to Buy:"), 0, 0);
        TextField txtSoldProductName = new TextField();
        salePanel.add(txtSoldProductName, 1, 0);

        // product count
        salePanel.add(new Label("How many ? "), 0, 1);
        TextField txtSellCount = new TextField();
        salePanel.add(txtSellCount, 1, 1);

        // add product
        Button btnBuy = new Button("Buy Product");
        salePanel.add(btnBuy, 1, 2);
        GridPane.setHalignment(btnBuy, HPos.LEFT);

        // status label
        Label sellStatus = new Label();
        GridPane.setColumnSpan(sellStatus, 2);
        salePanel.add(sellStatus, 0, 3);
        GridPane.setHalignment(sellStatus, HPos.CENTER);

        // add handler
        btnBuy.setOnAction(e -> {

            String productName = txtSoldProductName.getText();
            int newSoldCount = Integer.parseInt(txtSellCount.getText());

            Product product = null;
            for(Product prod: userProducts) {
                if(prod.getName().equals(productName)) {
                    product = prod;
                    break;
                }
            }
            assert product != null;
            int soldSoFar = product.getSoldCount() + newSoldCount;
            if(soldSoFar > product.getOriginalCount()) {
                return;
            }

            product.setSoldCount(soldSoFar);

            EventObject event = new EventObject(product);
            event.setStatusLabel(sellStatus);
            eventManager.notify(sold, event);

        });

        GridPane.setValignment(salePanel, VPos.CENTER);

        rootNode.add(salePanel, 0, 1);

        // ===========================
        // user view table
        // ===========================

        userView = new TableView<>();


        TableColumn<Product, String> productNameCol = new TableColumn<>("Name");
        TableColumn<Product, Integer> productCountCol = new TableColumn<>("In Store");

        // Defines how to fill data for each cell.
        // Get value from property of UserAccount. .

        productNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        productNameCol.setStyle("-fx-alignment: CENTER;");
        productCountCol.setCellValueFactory(new PropertyValueFactory<>("inStoreCount"));
        productCountCol.setStyle("-fx-alignment: CENTER;");

        // Set Sort type for userName column
        productNameCol.setSortType(TableColumn.SortType.DESCENDING);

        // Display row data
        userProducts = FXCollections.observableArrayList(dbManager.getProducts());
        userView.setItems(userProducts);

        //noinspection unchecked
        userView.getColumns().addAll(productNameCol, productCountCol);
        userView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        VBox userViewBox = new VBox();
        userViewBox.setAlignment(Pos.CENTER);
        userViewBox.getChildren().add(new Label("User View"));
        userViewBox.getChildren().add(userView);

        rootNode.add(userViewBox, 1, 0);

        Button btnSort = new Button("Sort Products");
        rootNode.add(btnSort, 1, 2);
        GridPane.setValignment(btnSort, VPos.BOTTOM);
        GridPane.setHalignment(btnSort, HPos.LEFT);

        // add handler
        btnSort.setOnAction(e -> {

            EventObject event = new EventObject(null);
            eventManager.notify(sorted, event);

        });

        GridPane.setRowSpan(userViewBox, 2);
        GridPane.setValignment(userViewBox, VPos.CENTER);
        GridPane.setHalignment(userViewBox, HPos.CENTER);

        // ===========================
        // executive view table
        // ===========================

        executiveView = new TableView<>();

        TableColumn<Product, String> productNameColExec = new TableColumn<>("Name");
        TableColumn<Product, Integer> productInStoreCol = new TableColumn<>("In Store");
        TableColumn<Product, Integer> productSoldCol = new TableColumn<>("Sold");


        // Defines how to fill data for each cell.
        // Get value from property of UserAccount. .

        productNameColExec.setCellValueFactory(new PropertyValueFactory<>("name"));
        productNameColExec.setStyle("-fx-alignment: CENTER;");
        productInStoreCol.setCellValueFactory(new PropertyValueFactory<>("inStoreCount"));
        productInStoreCol.setStyle("-fx-alignment: CENTER;");
        productSoldCol.setCellValueFactory(new PropertyValueFactory<>("soldCount"));
        productSoldCol.setStyle("-fx-alignment: CENTER;");

        for(TableColumn tc: executiveView.getColumns()) {
            tc.setSortable(false);
        }

        // Display row data
        executiveProducts = FXCollections.observableArrayList(dbManager.getProducts());
        executiveView.setItems(executiveProducts);

        //noinspection unchecked
        executiveView.getColumns().addAll(productNameColExec, productInStoreCol, productSoldCol);

        VBox execViewBox= new VBox();
        Label execViewLabel = new Label("Executive View");
        execViewBox.getChildren().add(execViewLabel);
        execViewBox.setAlignment(Pos.CENTER);
        execViewBox.getChildren().add(executiveView);

        executiveView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        rootNode.add(execViewBox, 2, 0);
        GridPane.setRowSpan(execViewBox, 2);
        GridPane.setValignment(execViewBox, VPos.CENTER);

        myStage.setScene(myScene);

        attachListeners();

        myStage.show();

    }

    @Override
    public void stop(){
        dbManager.closeDatabase();
    }

    private void attachListeners() {

        eventManager = new EventManager(added, sold, sorted);

        eventManager.subscribe(added, new ProductAddedDatabaseListener(dbManager));
        eventManager.subscribe(added, new ProductAddedUserViewListener(userView, userProducts));
        eventManager.subscribe(added, new ProductAddedExecutiveViewListener(executiveView, executiveProducts));

        eventManager.subscribe(sold, new ProductSoldDatabaseListener(dbManager));
        eventManager.subscribe(sold, new ProductSoldUserViewListener(userView, userProducts));
        eventManager.subscribe(sold, new ProductSoldExecutiveViewListener(executiveView, executiveProducts));

        eventManager.subscribe(sorted, new ProductsSortedListener(userView, userProducts));

    }

    public static void main(String[] args) {
        launch(args);
    }

    public static class ProductsDataController {

        private PreparedStatement insertStatement, updateStatement;
        private Connection conn;
        private ResultSet rs;
        private Statement stmt;

        ProductsDataController() {
            setUpDatabase();
        }

        private void setUpDatabase() {

            String createSQL = "create table product ("
                    + "id integer not null generated always as"
                    + " identity (start with 1, increment by 1), "
                    + "name varchar(30) not null, original int, sold int, "
                    + "constraint primary_key primary key (id))";

            try {
                Driver derbyEmbeddedDriver = new EmbeddedDriver();
                DriverManager.registerDriver(derbyEmbeddedDriver);
                conn = DriverManager.getConnection
                        ("jdbc:derby:database;create=true");
                stmt = conn.createStatement();
                conn.setAutoCommit(false);

                DatabaseMetaData dbm = conn.getMetaData();
                rs = dbm.getTables(null, "APP", "PRODUCT", null);
                if (rs.next()) {
                    System.out.println("Table exists");
                } else {
                    System.out.println("Table does not exist");
                    stmt.execute(createSQL);
                    conn.commit();

                }

                // prepare statement;
                insertStatement = conn.prepareStatement(
                        "insert into product (name, original, sold) values(?,?,?)");
                updateStatement = conn.prepareStatement(
                        "update product set sold = ? where name = ?");

            } catch (SQLException ex) {
                System.out.println("in connection" + ex);
            }

        }

        ArrayList<Product> getProducts() {
            ArrayList<Product> products = new ArrayList<>();
            try {
                rs = stmt.executeQuery("select * from product");
                while (rs.next()) {
                    Product product = new Product();
                    product.setName(rs.getString("name"));
                    product.setOriginalCount(rs.getInt("original"));
                    product.setSoldCount(rs.getInt("sold"));
                    products.add(product);
                }
                System.out.println("Products length: "+products.size());
                return products;
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }

        }

        boolean addProduct(Product product) {
            try {
                insertStatement.setString(1, product.getName());
                insertStatement.setInt(2, product.getOriginalCount());
                insertStatement.setInt(3, product.getSoldCount());
                insertStatement.executeUpdate();
                conn.commit();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        boolean updateProduct(Product product) {
            try {
                updateStatement.setInt(1, product.getSoldCount());
                updateStatement.setString(2, product.getName());
                updateStatement.executeUpdate();
                conn.commit();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

        void closeDatabase() {
            try {
                DriverManager.getConnection
                        ("jdbc:derby:;shutdown=true");
            } catch (SQLException ex) {
                if (((ex.getErrorCode() == 50000) &&
                        ("XJ015".equals(ex.getSQLState())))) {
                    System.out.println("Derby shut down normally");
                } else {
                    System.err.println("Derby did not shut down normally");
                    System.err.println(ex.getMessage());
                }
            }
        }

    }

    @SuppressWarnings("WeakerAccess")
    public static class Product {

        private String name;
        private int originalCount;
        private int soldCount;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getOriginalCount() {
            return originalCount;
        }

        public void setOriginalCount(int originalCount) {
            this.originalCount = originalCount;
        }

        public int getSoldCount() {
            return soldCount;
        }
        
        @SuppressWarnings("unused")
        public int getInStoreCount() {return originalCount - soldCount;}

        public void setSoldCount(int soldCount) {
            this.soldCount = soldCount;
        }
    }

    static class EventManager {

        Map<String, List<EventListener>> listeners = new HashMap<>();

        EventManager(String... operations) {
            for (String operation : operations) {
                this.listeners.put(operation, new ArrayList<>());
            }
        }

        void subscribe(String eventType, EventListener listener) {
            List<EventListener> users = listeners.get(eventType);
            users.add(listener);
        }


        void notify(String eventType, EventObject event) {
            List<EventListener> users = listeners.get(eventType);
            for (EventListener listener : users) {
                listener.update(event);
            }
        }
    }

    static class EventObject {

        Product product;
        String sortedBy = "Name";
        Label statusLabel;

        EventObject(Product product) {
            this.product = product;
        }

        void setStatusLabel(Label statusLabel) {
            this.statusLabel = statusLabel;
        }

    }

    interface EventListener {
        void update(EventObject event);
    }

    static class ProductAddedDatabaseListener implements EventListener {

        ProductsDataController controller;

        ProductAddedDatabaseListener(ProductsDataController controller) {
            this.controller = controller;
        }

        @Override
        public void update(EventObject event) {
           boolean success = controller.addProduct(event.product);
           if(success) {
               event.statusLabel.setText("Successfully Added");
           } else {
               event.statusLabel.setText("Not Added");
           }
        }
    }

    static class ProductAddedUserViewListener implements EventListener {

        ObservableList<Product> userViewProducts;
        TableView<Product> userView;

        ProductAddedUserViewListener(TableView<Product> userView,
                                     ObservableList<Product> userViewProducts) {
            this.userView = userView;
            this.userViewProducts = userViewProducts;
        }

        @Override
        public void update(EventObject event) {
            this.userViewProducts.add(event.product);
            this.userView.refresh();
        }
    }

    static class ProductAddedExecutiveViewListener implements EventListener {

        ObservableList<Product> executiveViewProducts;
        TableView<Product> executiveView;

        ProductAddedExecutiveViewListener(TableView<Product> executiveView,
                                          ObservableList<Product> executiveViewProducts) {
            this.executiveView = executiveView;
            this.executiveViewProducts = executiveViewProducts;
        }

        @Override
        public void update(EventObject event) {
            this.executiveViewProducts.add(event.product);
            this.executiveView.refresh();
        }
    }

    static class ProductSoldDatabaseListener implements EventListener {

        ProductsDataController controller;

        ProductSoldDatabaseListener(ProductsDataController controller) {
            this.controller = controller;
        }

        @Override
        public void update(EventObject event) {
            boolean success = controller.updateProduct(event.product);
            if(success) {
                event.statusLabel.setText("Successfully Bought");
            } else {
                event.statusLabel.setText("Not Bought");
            }
        }
    }

    static class ProductSoldUserViewListener implements EventListener {

        ObservableList<Product> userViewProducts;
        TableView<Product> userView;

        ProductSoldUserViewListener(TableView<Product> userView,
                                    ObservableList<Product> userViewProducts) {
            this.userView = userView;
            this.userViewProducts = userViewProducts;
        }

        @Override
        public void update(EventObject event) {
            for(Product prod: userViewProducts) {
                if(prod.getName().equalsIgnoreCase(event.product.getName())) {
                    prod.setSoldCount(event.product.getSoldCount());
                }
            }
            userView.refresh();
        }
    }

    static class ProductSoldExecutiveViewListener implements EventListener {

        ObservableList<Product> executiveViewProducts;
        TableView<Product> executiveView;

        ProductSoldExecutiveViewListener(TableView<Product> executiveView,
                                         ObservableList<Product> executiveViewProducts) {
            this.executiveView = executiveView;
            this.executiveViewProducts = executiveViewProducts;
        }

        @Override
        public void update(EventObject event) {
            for(Product prod: executiveViewProducts) {
                if(prod.getName().equalsIgnoreCase(event.product.getName())) {
                    prod.setSoldCount(event.product.getSoldCount());
                }
            }
            executiveView.refresh();
        }
    }

    static class ProductsSortedListener implements EventListener {

        ObservableList<Product> userViewProducts;
        TableView<Product> userView;

        ProductsSortedListener(TableView<Product> userView,
                               ObservableList<Product> userViewProducts) {
            this.userView = userView;
            this.userViewProducts = userViewProducts;
        }

        @Override
        public void update(EventObject event) {
            if(event.sortedBy.equalsIgnoreCase("Name")) {
                userViewProducts.sort(Comparator.comparing(Product::getName));
                userView.refresh();
            }
        }
    }

}


