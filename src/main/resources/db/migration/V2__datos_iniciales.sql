-- Plan moderado (50 / 30 / 20) y las bolsitas que definiste. Montos en cero:
-- se cargan desde la app con "Cargar operación".

INSERT INTO plan_inversion (perfil, objetivo_bajo, objetivo_medio, objetivo_alto, aporte_mensual)
VALUES ('MODERADO', 50, 30, 20, NULL);

INSERT INTO bolsita (nombre, tipo, entidad, tasa, meta, fecha_uso)
VALUES ('Black Friday', 'RESERVA', 'Mercado Pago', 0.19, 500000, DATE '2026-11-27');

INSERT INTO bolsita (nombre, tipo, entidad, tasa, meta, fecha_uso)
VALUES ('Emergencia', 'EMERGENCIA', 'Mercado Pago', 0.19, NULL, NULL);

INSERT INTO bolsita (nombre, tipo, entidad, tasa, meta, fecha_uso)
VALUES ('Enero en adelante', 'PLAZO_FIJO', NULL, NULL, NULL, NULL);
