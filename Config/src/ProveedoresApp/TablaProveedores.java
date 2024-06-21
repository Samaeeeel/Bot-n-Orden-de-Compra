package ProveedoresApp;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.*;
import javax.imageio.ImageIO;

public class TablaProveedores extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;
    private Connection connection;

    public TablaProveedores(Connection connection) {
        this.connection = connection;
        setTitle("Tabla de Proveedores");
        setSize(1920, 1080); // Tamaño para pantalla completa
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Pantalla completa
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(new Object[]{"Editar", "Código", "Nombre", "Identificación", "Dirección", "Teléfono", "Celular", "Email", "Tipo", "Status", "Foto", "Eliminar"}, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 10) {
                    return ImageIcon.class;
                } else {
                    return String.class;
                }
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0 || column == 11 || (column != 10 && column != 1); // Permite la edición en todas las columnas menos la de foto y código, y activa las columnas de los botones
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(50); // Ajuste de altura para ver mejor las imágenes
        table.getColumn("Editar").setCellRenderer(new ButtonRenderer());
        table.getColumn("Editar").setCellEditor(new ButtonEditor(new JCheckBox(), true));
        table.getColumn("Eliminar").setCellRenderer(new ButtonRenderer());
        table.getColumn("Eliminar").setCellEditor(new ButtonEditor(new JCheckBox(), false));

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        cargarDatos();
        setVisible(true);
    }

    private void cargarDatos() {
        try {
            String sql = "SELECT * FROM PROVEEDORES";
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] rowData = new Object[12];
                rowData[0] = "Editar";
                rowData[1] = rs.getString("PRVCODIGO");
                rowData[2] = rs.getString("PRVNOMBRE");
                rowData[3] = rs.getString("PRVIDENTIFICACION");
                rowData[4] = rs.getString("PRVDIRECCION");
                rowData[5] = rs.getString("PRVTELEFONO");
                rowData[6] = rs.getString("PRVCELULAR");
                rowData[7] = rs.getString("PRVEMAIL");
                rowData[8] = rs.getString("PRVTIPO");
                rowData[9] = rs.getString("PRVSTATUS");

                byte[] imgBytes = rs.getBytes("FOTO");
                if (imgBytes != null) {
                    ByteArrayInputStream bais = new ByteArrayInputStream(imgBytes);
                    Image img = ImageIO.read(bais);
                    ImageIcon icon = new ImageIcon(img.getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                    rowData[10] = icon;
                } else {
                    rowData[10] = null;
                }

                rowData[11] = "Eliminar";
                tableModel.addRow(rowData);
            }
        } catch (SQLException | IOException ex) {
            ex.printStackTrace();
        }
    }

    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isEditButton;
        private boolean clicked;

        public ButtonEditor(JCheckBox checkBox, boolean isEditButton) {
            super(checkBox);
            this.isEditButton = isEditButton;
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            clicked = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (clicked) {
                if (isEditButton) {
                    editarProveedor(table.getSelectedRow());
                } else {
                    eliminarProveedor(table.getSelectedRow());
                }
            }
            clicked = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            clicked = false;
            return super.stopCellEditing();
        }

        @Override
        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }

    private void editarProveedor(int row) {
        String codigo = (String) tableModel.getValueAt(row, 1);
        ProveedoresApp.getTxtCodigo().setText(codigo);
        ProveedoresApp.getTxtNombre().setText((String) tableModel.getValueAt(row, 2));
        ProveedoresApp.getTxtIdentificacion().setText((String) tableModel.getValueAt(row, 3));
        ProveedoresApp.getTxtDireccion().setText((String) tableModel.getValueAt(row, 4));
        ProveedoresApp.getTxtTelefono().setText((String) tableModel.getValueAt(row, 5));
        ProveedoresApp.getTxtCelular().setText((String) tableModel.getValueAt(row, 6));
        ProveedoresApp.getTxtEmail().setText((String) tableModel.getValueAt(row, 7));
        ProveedoresApp.getCmbTipo().setSelectedItem(tableModel.getValueAt(row, 8));
        ProveedoresApp.getCmbStatus().setSelectedItem(tableModel.getValueAt(row, 9));
        
        Icon imgIcon = (Icon) tableModel.getValueAt(row, 10);
        if (imgIcon != null) {
            ProveedoresApp.getLblImagenSeleccionada().setIcon(imgIcon);
        } else {
            ProveedoresApp.getLblImagenSeleccionada().setIcon(null);
        }
        
        // Mostrar la interfaz principal en primer plano
        ProveedoresApp.getFrame().toFront();
        ProveedoresApp.getFrame().requestFocus();
    }

    private void eliminarProveedor(int row) {
        String codigo = (String) tableModel.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(null, "¿Estás seguro de que deseas eliminar el proveedor con código " + codigo + "?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String sql = "DELETE FROM PROVEEDORES WHERE PRVCODIGO = ?";
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.setString(1, codigo);
                int rowsDeleted = stmt.executeUpdate();

                if (rowsDeleted > 0) {
                    JOptionPane.showMessageDialog(null, "Proveedor eliminado correctamente");
                    tableModel.removeRow(row);
                } else {
                    JOptionPane.showMessageDialog(null, "No se eliminó el proveedor. Verifica los datos e intenta nuevamente.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error al eliminar proveedor: " + ex.getMessage());
            }
        }
    }
}
