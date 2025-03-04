import { useEffect, useState } from 'react';
import Layout from '../../components/Layout';
import { adminApi } from '../../api';
import '../../styles/admin.css';

const DEFAULT_FORM = {
  name: '',
  deptId: ''
};