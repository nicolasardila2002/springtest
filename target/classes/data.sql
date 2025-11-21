use logitrack_db;

INSERT INTO usuario (nombre, email, password_hash, rol) VALUES
('Admin General', 'admin@empresa.com', 'adminpass123', 'ADMIN'),
('Encargado Almacén 1', 'encargado1@empresa.com', 'passencargado1', 'EMPLEADO'),
('Encargado Almacén 2', 'encargado2@empresa.com', 'passencargado2', 'EMPLEADO'),
('Empleado Logística', 'empleado@empresa.com', 'passemp', 'EMPLEADO'),
('Super Admin', 'superadmin@empresa.com', 'supersecure', 'ADMIN');

INSERT INTO bodega (nombre, ubicacion, capacidad, encargado_id) VALUES
('Almacén Principal', 'Calle 10 # 5-20, Centro', 5000, 2),
('Bodega Norte', 'Autopista Norte Km 15', 3500, 3),
('Bodega Sur', 'Avenida Sur # 2-50', 2000, 2),
('Almacén de Devoluciones', 'Zona Industrial, Lote 4', 1000, 3),
('Bodega Express', 'Carrera 7 # 1-10', 500, 4);

INSERT INTO producto (nombre, categoria, precio) VALUES
('Laptop Pro X', 'Electrónica', 1250.00),
('Monitor LED 27"', 'Electrónica', 350.50),
('Silla Ergonómica', 'Mobiliario', 180.99),
('Resma Papel A4', 'Oficina', 5.75),
('Kit de Herramientas Básico', 'Ferretería', 75.00);

INSERT INTO inventario (producto_id, bodega_id, stock_actual) VALUES
(1, 1, 50),  -- Laptop Pro X en Almacén Principal
(2, 2, 120), -- Monitor LED en Bodega Norte
(3, 1, 80),  -- Silla Ergonómica en Almacén Principal
(4, 3, 300), -- Resma Papel A4 en Bodega Sur
(5, 2, 65);  -- Kit de Herramientas en Bodega Norte

INSERT INTO movimiento_inventario (fecha, tipo, usuario_id, bodega_origen_id, bodega_destino_id) VALUES
('2025-11-13 10:00:00', 'ENTRADA', 1, NULL, 1),
('2025-11-13 11:30:00', 'SALIDA', 4, 1, NULL),
('2025-11-13 14:00:00', 'TRANSFERENCIA', 2, 2, 3),
('2025-11-14 09:00:00', 'ENTRADA', 1, NULL, 2),
('2025-11-14 10:30:00', 'SALIDA', 4, 3, NULL);

INSERT INTO movimiento_detalle (movimiento_id, producto_id, cantidad) VALUES
(1, 1, 20), -- Entrada de 20 Laptop Pro X
(2, 3, 5),  -- Salida de 5 Silla Ergonómica
(3, 2, 10), -- Transferencia de 10 Monitor LED
(4, 5, 30), -- Entrada de 30 Kit de Herramientas
(5, 4, 50); -- Salida de 50 Resma Papel A4

INSERT INTO auditoria_cambios (fecha_hora, usuario_id, tipo_operacion, entidad_afectada, id_entidad_afectada, valores_antes, valores_despues) VALUES
('2025-11-13 09:50:00', 1, 'INSERT', 'usuario', 6, NULL, '{"nombre": "Nuevo Usuario", "rol": "empleado"}'),
('2025-11-13 10:15:00', 1, 'UPDATE', 'bodega', 1, '{"capacidad": 5000}', '{"capacidad": 5500}'),
('2025-11-13 12:00:00', 2, 'INSERT', 'producto', 6, NULL, '{"nombre": "Mouse Inalámbrico", "precio": 25.00}'),
('2025-11-13 15:30:00', 1, 'DELETE', 'inventario', 101, '{"producto_id": 4, "bodega_id": 3}', NULL),
('2025-11-14 11:00:00', 3, 'UPDATE', 'producto', 4, '{"precio": 5.75}', '{"precio": 6.00}');