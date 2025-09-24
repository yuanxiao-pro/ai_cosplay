export default {
  getItem(key) {
    return new Promise((resolve, reject) => {
      try {
        const value = localStorage.getItem(key);
        // localStorage.getItem返回null时表示没有找到，这里保持一致性
        resolve(value);
      } catch (err) {
        reject(err);
      }
    });
  },
  setItem(key, value) {
    return new Promise((resolve, reject) => {
      try {
        // localStorage只能存储字符串，如果是对象需要序列化
        const dataToStore = typeof value === 'string' ? value : JSON.stringify(value);
        localStorage.setItem(key, dataToStore);
        resolve();
      } catch (err) {
        reject(err);
      }
    });
  },
  removeItem(key) {
    return new Promise((resolve, reject) => {
      try {
        localStorage.removeItem(key);
        resolve();
      } catch (err) {
        reject(err);
      }
    });
  },
};