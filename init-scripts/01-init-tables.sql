-- Create a test table for sql2rest application
CREATE TABLE sql2rest_test (
  id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name VARCHAR2(100) NOT NULL,
  description VARCHAR2(255),
  created_date DATE DEFAULT SYSDATE
);

-- Insert some sample data
INSERT INTO sql2rest_test (name, description) VALUES ('Test 1', 'First test record');
INSERT INTO sql2rest_test (name, description) VALUES ('Test 2', 'Second test record');
INSERT INTO sql2rest_test (name, description) VALUES ('Test 3', 'Third test record');

COMMIT;

-- Grant permissions to the app user
GRANT SELECT, INSERT, UPDATE, DELETE ON sql2rest_test TO sql2rest;
