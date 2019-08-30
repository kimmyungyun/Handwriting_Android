import tensorflow as tf
from keras.models import load_model
import os

h5_path = 'summary.h5'
out_path = 'model.tflite'
def save_model():
    model = tf.keras.Sequential()
    model = load_model('Model.h5')
    model.load_weights("Weights.h5")

    model.save(h5_path)

    converter = tf.lite.TFLiteConverter.from_keras_model_file(h5_path)
    flat_data = converter.convert()

    with open(out_path, 'wb') as f:
        f.write(flat_data)


save_model()