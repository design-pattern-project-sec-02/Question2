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
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.apache.derby.jdbc.EmbeddedDriver;

import java.sql.*;
import java.util.ArrayList;

public class ProductsManager extends Application {

    private ProductsDataController dbManager;
    private ObservableList<Product> products;
    private TableView<Product> userView;

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
            product.setShippingCount(count);
            
            boolean success = dbManager.addProduct(product);

            if(success) {
                products.add(product);
                addStatus.setText("Successfully added!");
            } else {
                addStatus.setText("Not added!");
            }

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
            int count = Integer.parseInt(txtSellCount.getText());

            Product targetProduct = null;
            for(Product product: products) {
                if(product.getName().equalsIgnoreCase(productName.trim())) {
                    targetProduct = product;
                    break;
                }
            }
            assert targetProduct != null;
            int salesCount = targetProduct.getSalesCount() + count;
            targetProduct.setSalesCount(salesCount);

            userView.refresh();

            boolean success = dbManager.updateProduct(targetProduct);

            if(success) {
                sellStatus.setText("Successfully Bought!");
            } else {
                sellStatus.setText("Not bought!");
            }

        });

        GridPane.setValignment(salePanel, VPos.CENTER);

        rootNode.add(salePanel, 0, 1);

        // ===========================
        // user view table
        // ===========================

        userView = new TableView<>();

        TableColumn<Product, String> productNameCol = new TableColumn<>("Name");
        TableColumn<Product, Integer> productCountCol = new TableColumn<>("Count");

        // Defines how to fill data for each cell.
        // Get value from property of UserAccount. .

        productNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        productCountCol.setCellValueFactory(new PropertyValueFactory<>("salesCount"));

        // Set Sort type for userName column
        productNameCol.setSortType(TableColumn.SortType.DESCENDING);

        // Display row data
        products = FXCollections.observableArrayList(dbManager.getProducts());
        userView.setItems(products);

        //noinspection unchecked
        userView.getColumns().addAll(productNameCol, productCountCol);
        userView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        StackPane tableRootUser = new StackPane();
        tableRootUser.getChildren().add(userView);

        rootNode.add(tableRootUser, 1, 0);
        GridPane.setRowSpan(tableRootUser, 2);
        GridPane.setValignment(tableRootUser, VPos.CENTER);

        // ===========================
        // executive view table
        // ===========================

        TableView<Product> executiveView = new TableView<>();

        TableColumn<Product, String> productNameColExec = new TableColumn<>("Name");
        TableColumn<Product, Integer> productShippingCol = new TableColumn<>("Shipping");

        // Defines how to fill data for each cell.
        // Get value from property of UserAccount. .

        productNameColExec.setCellValueFactory(new PropertyValueFactory<>("name"));
        productShippingCol.setCellValueFactory(new PropertyValueFactory<>("shippingCount"));

        // Set Sort type for userName column
        productNameColExec.setSortType(TableColumn.SortType.DESCENDING);

        // Display row data
        executiveView.setItems(products);

        //noinspection unchecked
        executiveView.getColumns().addAll(productNameColExec, productShippingCol);

        StackPane tableRootExec = new StackPane();
        tableRootExec.getChildren().add(executiveView);

        executiveView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        rootNode.add(tableRootExec, 2, 0);
        GridPane.setRowSpan(tableRootExec, 2);
        GridPane.setValignment(tableRootExec, VPos.CENTER);

        myStage.setScene(myScene);

        myStage.show();

    }

    @Override
    public void stop(){
        dbManager.closeDatabase();
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
                    + "name varchar(30) not null, shipping int, sales int, "
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
                        "insert into product (name, shipping, sales) values(?,?,?)");
                updateStatement = conn.prepareStatement(
                        "update product set sales = ? where name = ?");

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
                    product.setShippingCount(rs.getInt("shipping"));
                    product.setSalesCount(rs.getInt("sales"));
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
                insertStatement.setInt(2, product.getShippingCount());
                insertStatement.setInt(3, product.getSalesCount());
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
                updateStatement.setInt(1, product.getSalesCount());
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
        private int shippingCount;
        private int salesCount;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getShippingCount() {
            return shippingCount;
        }

        public void setShippingCount(int shippingCount) {
            this.shippingCount = shippingCount;
        }

        public int getSalesCount() {
            return salesCount;
        }

        public void setSalesCount(int salesCount) {
            this.salesCount = salesCount;
        }
    }

}


