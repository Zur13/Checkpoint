/*
 * Copyright 2018 Yurii Polianytsia (coolio-iglesias@yandex.ru)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package test.zur13.checkpoint.test;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableModel;

import zur13.checkpoint.ACheckpoint;
import zur13.checkpoint.CheckpointBuilder;
import zur13.checkpoint.Pass;

public class Tester {
	private static final int RES_APPLIC_COL = 0;
	private static final int RES_THRDIN_COL = 1;
	private static final int RES_THRDWT_COL = 2;
	private static final int RES_MAXTHR_COL = 3;

	private JFrame frmCheclpointTester;
	private JTable table;
	private JTextField txtThreads;
	private JTextField txtApplicants;
	private JTextField txtApplicantRO;

	private int threadsCnt = 10;
	private int applicants = 10;
	private int roPasses = 10;

	private TesterThread[] threads;

	private AtomicLong[][] results;

	private ACheckpoint checkpoint;

	public void btnStartClick() {
		parseInput();
		initTester();
	}

	public void btnStopClick() {
		for (TesterThread t : threads) {
			t.setStopRequested(true);
		}
	}

	public void parseInput() {
		threadsCnt = Integer.parseInt(txtThreads.getText());
		applicants = Integer.parseInt(txtApplicants.getText());
		roPasses = Integer.parseInt(txtApplicantRO.getText());
	}

	public void initTester() {
		threads = new TesterThread[threadsCnt];
		results = new AtomicLong[applicants][5];
		checkpoint = CheckpointBuilder.newInst().setMaxPassesPerResource(roPasses).setReentrant(true).build();
		int i = 0;
		for (AtomicLong[] resRow : results) {
			for (int j = 0; j < resRow.length; j++) {
				resRow[j] = new AtomicLong(0);
			}
			resRow[0].set(i);
			i++;
		}

		for (i = 0; i < threads.length; i++) {
			threads[i] = new TesterThread(i % applicants);
			threads[i].start();
		}

		table.setModel(new DefaultTableModel(results,
				new String[] { "Applicant ID", "Threads In Section", "Threads Waiting", "Max Threads In" }) {
			Class[] columnTypes = new Class[] { Long.class, Long.class, Long.class, Long.class };

			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
		});
	}

	public long getInitialReenter() {
		return roPasses + 1;
	}

	public void test(Long resourceId, boolean ro, Long reenterCounter) {
		AtomicLong[] row = results[resourceId.intValue()];
		row[RES_THRDWT_COL].incrementAndGet();

		if ( ro ) {
			try (Pass p = checkpoint.getPass(resourceId)) {
				row[RES_THRDWT_COL].decrementAndGet();
				if ( reenterCounter == getInitialReenter() ) {
					row[RES_THRDIN_COL].incrementAndGet();
				}

				testPayload(resourceId, ro);
				if ( reenterCounter > 0 ) {
					test(resourceId, ro, reenterCounter - 1);
				}

				if ( row[RES_THRDIN_COL].get() > roPasses ) {
					int i = 0;
					i++;
				}

				if ( reenterCounter == getInitialReenter() ) {
					row[RES_THRDIN_COL].decrementAndGet();
				}

			} catch (Exception e) {
				row[RES_THRDWT_COL].decrementAndGet();
				e.printStackTrace(System.err);
			}
		} else {
			try (Pass p = checkpoint.getPassRW(resourceId)) {
				row[RES_THRDWT_COL].decrementAndGet();
				if ( reenterCounter == getInitialReenter() ) {
					row[RES_THRDIN_COL].incrementAndGet();
				}

				testPayload(resourceId, ro);
				if ( reenterCounter > 0 ) {
					test(resourceId, ro, reenterCounter - 1);
				}
				
				if ( row[RES_THRDIN_COL].get() > 1 ) {
					int i = 0;
					i++;
				}

				if ( reenterCounter == getInitialReenter() ) {
					row[RES_THRDIN_COL].decrementAndGet();
				}
			} catch (Exception e) {
				row[RES_THRDWT_COL].decrementAndGet();
				e.printStackTrace(System.err);
			}
		}

	}

	public void testPayload(Long resourceId, boolean ro) throws Exception {
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		AtomicLong[] row = results[resourceId.intValue()];

		long cur = row[RES_THRDIN_COL].get();
		long max = row[RES_MAXTHR_COL].get();
		while ( cur > max && !row[RES_MAXTHR_COL].compareAndSet(max, cur) ) {
			max = row[RES_MAXTHR_COL].get();
		}
		model.fireTableDataChanged();
		table.repaint();

		Thread.sleep(1 * 50);

		model.fireTableDataChanged();
		table.repaint();
	}

	public class TesterThread extends Thread {
		long resourceId;
		boolean stopRequested = false;

		public TesterThread(long resourceId) {
			super();
			this.resourceId = resourceId;
		}

		@Override
		public void run() {
			long i = 0;
			while ( !stopRequested ) {
				if ( i % 11 != 10 ) {
					test(resourceId, true, getInitialReenter());
				} else {
					test(resourceId, false, getInitialReenter());
				}
				i++;
			}
		}

		public void setStopRequested(boolean stopRequested) {
			this.stopRequested = stopRequested;
			interrupt();
		}
	}

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Tester window = new Tester();
					window.frmCheclpointTester.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Tester() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmCheclpointTester = new JFrame();
		frmCheclpointTester.setTitle("Checkpoint Tester");
		frmCheclpointTester.setBounds(100, 100, 1024, 625);
		frmCheclpointTester.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel panel = new JPanel();
		frmCheclpointTester.getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(3, 3));

		JPanel panel_1 = new JPanel();
		panel_1.setBounds(new Rectangle(5, 5, 5, 5));
		panel_1.setBorder(
				new CompoundBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), new EmptyBorder(5, 5, 5, 5)));
		panel.add(panel_1, BorderLayout.WEST);
		panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.Y_AXIS));

		JPanel panel_3 = new JPanel();
		panel_1.add(panel_3);
		panel_3.setLayout(new BoxLayout(panel_3, BoxLayout.X_AXIS));

		JLabel lblThreads = new JLabel("Threads");
		lblThreads.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel_3.add(lblThreads);

		Component rigidArea_2 = Box.createRigidArea(new Dimension(20, 20));
		panel_3.add(rigidArea_2);

		txtThreads = new JTextField();
		txtThreads.setMaximumSize(new Dimension(200, 24));
		txtThreads.setText("40");
		panel_3.add(txtThreads);
		txtThreads.setColumns(5);

		Component rigidArea_3 = Box.createRigidArea(new Dimension(20, 20));
		panel_1.add(rigidArea_3);

		JPanel panel_4 = new JPanel();
		panel_1.add(panel_4);
		panel_4.setLayout(new BoxLayout(panel_4, BoxLayout.X_AXIS));

		JLabel lblApplicants = new JLabel("Applicants");
		lblApplicants.setAlignmentX(0.5f);
		panel_4.add(lblApplicants);

		Component rigidArea_1 = Box.createRigidArea(new Dimension(20, 20));
		panel_4.add(rigidArea_1);

		txtApplicants = new JTextField();
		txtApplicants.setText("10");
		txtApplicants.setMaximumSize(new Dimension(200, 24));
		txtApplicants.setColumns(5);
		panel_4.add(txtApplicants);

		Component rigidArea_5 = Box.createRigidArea(new Dimension(20, 20));
		panel_1.add(rigidArea_5);

		JPanel panel_5 = new JPanel();
		panel_1.add(panel_5);
		panel_5.setLayout(new BoxLayout(panel_5, BoxLayout.X_AXIS));

		JLabel lblApplicantRoPasses = new JLabel("RO Passes Per Applicant");
		lblApplicantRoPasses.setAlignmentX(0.5f);
		panel_5.add(lblApplicantRoPasses);

		Component rigidArea_6 = Box.createRigidArea(new Dimension(20, 20));
		panel_5.add(rigidArea_6);

		txtApplicantRO = new JTextField();
		txtApplicantRO.setText("3");
		txtApplicantRO.setMaximumSize(new Dimension(200, 24));
		txtApplicantRO.setColumns(5);
		panel_5.add(txtApplicantRO);

		Component rigidArea_4 = Box.createRigidArea(new Dimension(20, 20));
		panel_1.add(rigidArea_4);

		JPanel panel_6 = new JPanel();
		panel_1.add(panel_6);
		panel_6.setLayout(new BoxLayout(panel_6, BoxLayout.X_AXIS));

		JButton btnStart = new JButton("Start");
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnStartClick();
			}
		});
		panel_6.add(btnStart);

		JButton btnStop = new JButton("Stop");
		panel_6.add(btnStop);
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnStopClick();
			}
		});

		Component rigidArea = Box.createRigidArea(new Dimension(20, 20));
		panel_1.add(rigidArea);

		Component verticalGlue = Box.createVerticalGlue();
		panel_1.add(verticalGlue);

		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel.add(panel_2, BorderLayout.CENTER);
		panel_2.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setPreferredSize(new Dimension(40, 40));
		panel_2.add(scrollPane, BorderLayout.CENTER);

		table = new JTable();
		scrollPane.setViewportView(table);
		table.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		table.setFillsViewportHeight(true);
		table.setModel(new DefaultTableModel(new Object[][] { { null, null, null, null }, },
				new String[] { "Applicant ID", "Threads In Section", "Threads Waiting", "Max Threads In" }) {
			Class[] columnTypes = new Class[] { Long.class, Long.class, Long.class, Long.class };

			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
		});
		table.getColumnModel().getColumn(1).setPreferredWidth(121);
		table.getColumnModel().getColumn(2).setPreferredWidth(108);
		table.getColumnModel().getColumn(3).setPreferredWidth(102);
	}

}
